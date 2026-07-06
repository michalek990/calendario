package com.calendario.hrnest.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.DuplicateProjectNameException;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectActionException;
import com.calendario.hrnest.domain.user.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProjectUseCaseTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private CreateProjectUseCase useCase() {
        return new CreateProjectUseCase(projectRepository, currentUserProvider);
    }

    @Test
    void execute_createsProject_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(projectRepository.existsByName("Kalendario")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectView view = useCase().execute(new CreateProjectCommand("Kalendario", "Aplikacja HR"));

        assertThat(view.name()).isEqualTo("Kalendario");
        assertThat(view.description()).isEqualTo("Aplikacja HR");
    }

    @Test
    void execute_createsProject_whenCallerIsAdmin() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(projectRepository.existsByName("Kalendario")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(useCase().execute(new CreateProjectCommand("Kalendario", null)).name()).isEqualTo("Kalendario");
    }

    @Test
    void execute_throwsForbidden_whenCallerIsManager() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);

        assertThatThrownBy(() -> useCase().execute(new CreateProjectCommand("Kalendario", null)))
                .isInstanceOf(ForbiddenProjectActionException.class);
    }

    @Test
    void execute_throwsDuplicateName_whenProjectAlreadyExists() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(projectRepository.existsByName("Kalendario")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(new CreateProjectCommand("Kalendario", null)))
                .isInstanceOf(DuplicateProjectNameException.class);
    }
}
