package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyOpenException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClockInUseCaseTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_createsOpenEntry_whenNoneIsOpen() {
        ClockInUseCase useCase = new ClockInUseCase(timeEntryRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.findOpenEntryByUserId(1L)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase.execute();

        assertThat(view.userId()).isEqualTo(1L);
        assertThat(view.clockOut()).isNull();
    }

    @Test
    void execute_throwsAlreadyOpen_whenOpenEntryExists() {
        ClockInUseCase useCase = new ClockInUseCase(timeEntryRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.findOpenEntryByUserId(1L)).thenReturn(Optional.of(TimeEntry.clockIn(1L)));

        assertThatThrownBy(useCase::execute).isInstanceOf(TimeEntryAlreadyOpenException.class);
    }
}
