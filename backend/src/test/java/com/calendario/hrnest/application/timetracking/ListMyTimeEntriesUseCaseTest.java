package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyTimeEntriesUseCaseTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ListMyTimeEntriesUseCase useCase() {
        return new ListMyTimeEntriesUseCase(timeEntryRepository, currentUserProvider);
    }

    @Test
    void execute_returnsOnlyEntriesOfCurrentUser() {
        TimeEntry entry = TimeEntry.reconstitute(1L, 7L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 30, null);

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(timeEntryRepository.findByUserId(7L)).thenReturn(List.of(entry));

        List<TimeEntryView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(7L);
        assertThat(result.get(0).totalMinutes()).isEqualTo(8 * 60 - 30);
    }

    @Test
    void execute_returnsEmptyList_whenUserHasNoEntries() {
        when(currentUserProvider.currentUserId()).thenReturn(9L);
        when(timeEntryRepository.findByUserId(9L)).thenReturn(List.of());

        assertThat(useCase().execute()).isEmpty();
    }
}
