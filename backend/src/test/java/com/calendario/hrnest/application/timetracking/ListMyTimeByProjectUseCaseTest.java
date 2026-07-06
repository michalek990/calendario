package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.project.ProjectTimeSummaryView;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyTimeByProjectUseCaseTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_groupsOwnClosedEntriesByProject() {
        ListMyTimeByProjectUseCase useCase =
                new ListMyTimeByProjectUseCase(timeEntryRepository, projectRepository, currentUserProvider);

        TimeEntry projectAEntry1 = TimeEntry.reconstitute(1L, 7L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, 1L);
        TimeEntry projectAEntry2 = TimeEntry.reconstitute(2L, 7L,
                Instant.parse("2026-01-02T08:00:00Z"), Instant.parse("2026-01-02T12:00:00Z"), 0, null, 1L);
        TimeEntry noProjectEntry = TimeEntry.reconstitute(3L, 7L,
                Instant.parse("2026-01-03T08:00:00Z"), Instant.parse("2026-01-03T10:00:00Z"), 0, null, null);
        TimeEntry openEntry = TimeEntry.reconstitute(4L, 7L, Instant.parse("2026-01-04T08:00:00Z"), null, 0, null, 1L);

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(timeEntryRepository.findByUserId(7L))
                .thenReturn(List.of(projectAEntry1, projectAEntry2, noProjectEntry, openEntry));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(
                Project.reconstitute(1L, "Kalendario", null, Instant.now())));

        List<ProjectTimeSummaryView> result = useCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).projectId()).isEqualTo(1L);
        assertThat(result.get(0).projectName()).isEqualTo("Kalendario");
        assertThat(result.get(0).totalMinutes()).isEqualTo(8 * 60 + 4 * 60);
        assertThat(result.get(0).entryCount()).isEqualTo(2);
    }

    @Test
    void execute_fallsBackToPlaceholderName_whenProjectWasDeleted() {
        ListMyTimeByProjectUseCase useCase =
                new ListMyTimeByProjectUseCase(timeEntryRepository, projectRepository, currentUserProvider);

        TimeEntry entry = TimeEntry.reconstitute(1L, 7L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, 99L);

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(timeEntryRepository.findByUserId(7L)).thenReturn(List.of(entry));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(useCase.execute().get(0).projectName()).isEqualTo("(usunięty projekt)");
    }
}
