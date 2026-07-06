package com.calendario.hrnest.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListProjectsUseCaseTest {

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void execute_returnsProjects_sortedByName() {
        ListProjectsUseCase useCase = new ListProjectsUseCase(projectRepository);

        when(projectRepository.findAll()).thenReturn(List.of(
                Project.create("Zebra", null),
                Project.create("Alfa", null)));

        List<ProjectView> result = useCase.execute();

        assertThat(result).extracting(ProjectView::name).containsExactly("Alfa", "Zebra");
    }
}
