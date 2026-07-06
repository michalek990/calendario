package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.project.ProjectTimeSummaryView;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Własne podsumowanie czasu pracy pogrupowane po projekcie — tylko wpisy z przypisanym projektem. */
@Component
public class ListMyTimeByProjectUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListMyTimeByProjectUseCase(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<ProjectTimeSummaryView> execute() {
        Long userId = currentUserProvider.currentUserId();

        Map<Long, List<TimeEntry>> entriesByProject = timeEntryRepository.findByUserId(userId).stream()
                .filter(entry -> !entry.isOpen())
                .filter(entry -> entry.getProjectId() != null)
                .collect(Collectors.groupingBy(TimeEntry::getProjectId));

        return entriesByProject.entrySet().stream()
                .map(entry -> toView(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ProjectTimeSummaryView::projectName))
                .toList();
    }

    private ProjectTimeSummaryView toView(Long projectId, List<TimeEntry> entries) {
        long totalMinutes = entries.stream().mapToLong(TimeEntry::totalMinutes).sum();
        String projectName = projectRepository.findById(projectId)
                .map(Project::getName)
                .orElse("(usunięty projekt)");

        return new ProjectTimeSummaryView(projectId, projectName, totalMinutes, entries.size());
    }
}
