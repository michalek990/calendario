package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyOpenException;
import org.springframework.stereotype.Component;

@Component
public class ClockInUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    public ClockInUseCase(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository,
                           CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public TimeEntryView execute() {
        return execute(null);
    }

    /** Rozpoczyna wpis, opcjonalnie przypisany do projektu (walidowany, jeśli podany). */
    public TimeEntryView execute(Long projectId) {
        Long userId = currentUserProvider.currentUserId();

        if (timeEntryRepository.findOpenEntryByUserId(userId).isPresent()) {
            throw new TimeEntryAlreadyOpenException();
        }

        if (projectId != null && projectRepository.findById(projectId).isEmpty()) {
            throw new ProjectNotFoundException(projectId);
        }

        TimeEntry saved = timeEntryRepository.save(TimeEntry.clockIn(userId, projectId));

        return TimeEntryView.from(saved);
    }
}
