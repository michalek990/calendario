package com.calendario.hrnest.application.project;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.DuplicateProjectNameException;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectActionException;
import com.calendario.hrnest.domain.user.Role;
import org.springframework.stereotype.Component;

@Component
public class CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    public CreateProjectUseCase(ProjectRepository projectRepository, CurrentUserProvider currentUserProvider) {
        this.projectRepository = projectRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public ProjectView execute(CreateProjectCommand command) {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenProjectActionException();
        }

        if (projectRepository.existsByName(command.name())) {
            throw new DuplicateProjectNameException(command.name());
        }

        Project saved = projectRepository.save(Project.create(command.name(), command.description()));

        return ProjectView.from(saved);
    }
}
