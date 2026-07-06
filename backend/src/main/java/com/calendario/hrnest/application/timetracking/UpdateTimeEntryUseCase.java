package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.common.FacilityScope;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryActionException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Poprawia godziny/przerwę/projekt już zarejestrowanego wpisu — np. gdy
 * clock-in/out zapisał złą godzinę. `HR` może poprawiać wyłącznie wpisy
 * pracowników swojego zakładu — `MANAGER`/`ADMIN` nie podlegają temu
 * ograniczeniu.
 */
@Component
public class UpdateTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public UpdateTimeEntryUseCase(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository,
                                   UserRepository userRepository, CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public TimeEntryView execute(UpdateTimeEntryCommand command) {
        TimeEntry timeEntry = timeEntryRepository.findById(command.timeEntryId())
                .orElseThrow(() -> new TimeEntryNotFoundException(command.timeEntryId()));

        boolean isOwnEntry = timeEntry.getUserId().equals(currentUserProvider.currentUserId());
        Role role = currentUserProvider.currentUserRole();
        boolean canManageAnyEntry = role == Role.HR || role == Role.MANAGER || role == Role.ADMIN;
        if (!isOwnEntry && !canManageAnyEntry) {
            throw new ForbiddenTimeEntryActionException();
        }

        if (!isOwnEntry && role == Role.HR
                && !FacilityScope.isSameFacility(userRepository, currentUserProvider.currentUserId(),
                        timeEntry.getUserId())) {
            throw new ForbiddenTimeEntryActionException();
        }

        if (command.projectId() != null && projectRepository.findById(command.projectId()).isEmpty()) {
            throw new ProjectNotFoundException(command.projectId());
        }

        int breakMinutes = command.breakMinutes() != null ? command.breakMinutes() : 0;

        TimeEntry updated = timeEntryRepository.save(
                timeEntry.update(command.clockIn(), command.clockOut(), breakMinutes, command.projectId()));

        return TimeEntryView.from(updated);
    }
}
