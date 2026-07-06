package com.calendario.hrnest.application.project;

import com.calendario.hrnest.domain.project.ProjectRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListProjectsUseCase {

    private final ProjectRepository projectRepository;

    public ListProjectsUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectView> execute() {
        return projectRepository.findAll().stream()
                .map(ProjectView::from)
                .sorted(Comparator.comparing(ProjectView::name))
                .toList();
    }
}
