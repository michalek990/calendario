package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import org.springframework.stereotype.Component;

/**
 * Rejestruje czas pracy na podstawie podanych chwil rozpoczęcia/zakończenia —
 * zamiast "na żywo" przez clock-in/clock-out.
 */
@Component
public class LogTimeEntryUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    public LogTimeEntryUseCase(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository,
                                CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public TimeEntryView execute(LogTimeEntryCommand command) {
        if (command.projectId() != null && projectRepository.findById(command.projectId()).isEmpty()) {
            throw new ProjectNotFoundException(command.projectId());
        }

        Long userId = currentUserProvider.currentUserId();
        int breakMinutes = command.breakMinutes() != null ? command.breakMinutes() : 0;

        TimeEntry saved = timeEntryRepository.save(TimeEntry.log(
                userId, command.clockIn(), command.clockOut(), breakMinutes, command.projectId()));

        return TimeEntryView.from(saved);
    }
}
