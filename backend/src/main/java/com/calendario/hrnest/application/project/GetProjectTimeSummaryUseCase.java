package com.calendario.hrnest.application.project;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectSummaryAccessException;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.user.Role;
import java.util.List;
import org.springframework.stereotype.Component;

/** Zbiorcze podsumowanie czasu pracy nad projektem — obejmuje wpisy wszystkich pracowników. */
@Component
public class GetProjectTimeSummaryUseCase {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final CurrentUserProvider currentUserProvider;

    public GetProjectTimeSummaryUseCase(ProjectRepository projectRepository, TimeEntryRepository timeEntryRepository,
                                         CurrentUserProvider currentUserProvider) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public ProjectTimeSummaryView execute(Long projectId) {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenProjectSummaryAccessException();
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<TimeEntry> closedEntries = timeEntryRepository.findByProjectId(projectId).stream()
                .filter(entry -> !entry.isOpen())
                .toList();

        long totalMinutes = closedEntries.stream().mapToLong(TimeEntry::totalMinutes).sum();

        return new ProjectTimeSummaryView(project.getId(), project.getName(), totalMinutes, closedEntries.size());
    }
}
