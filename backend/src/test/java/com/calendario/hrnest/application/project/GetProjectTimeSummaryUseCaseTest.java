package com.calendario.hrnest.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectSummaryAccessException;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.user.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProjectTimeSummaryUseCaseTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private GetProjectTimeSummaryUseCase useCase() {
        return new GetProjectTimeSummaryUseCase(projectRepository, timeEntryRepository, currentUserProvider);
    }

    @Test
    void execute_sumsClosedEntries_acrossAllUsers() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(Project.reconstitute(1L, "Kalendario", null, Instant.now())));

        TimeEntry closedA = TimeEntry.reconstitute(1L, 10L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, 1L);
        TimeEntry closedB = TimeEntry.reconstitute(2L, 11L,
                Instant.parse("2026-01-02T08:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"), 0, null, 1L);
        TimeEntry open = TimeEntry.reconstitute(3L, 12L, Instant.parse("2026-01-03T08:00:00Z"), null, 0, null, 1L);

        when(timeEntryRepository.findByProjectId(1L)).thenReturn(List.of(closedA, closedB, open));

        ProjectTimeSummaryView view = useCase().execute(1L);

        assertThat(view.projectName()).isEqualTo("Kalendario");
        assertThat(view.totalMinutes()).isEqualTo(8 * 60 + 4 * 60);
        assertThat(view.entryCount()).isEqualTo(2);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(ForbiddenProjectSummaryAccessException.class);
    }

    @Test
    void execute_throwsNotFound_whenProjectDoesNotExist() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(404L)).isInstanceOf(ProjectNotFoundException.class);
    }
}
