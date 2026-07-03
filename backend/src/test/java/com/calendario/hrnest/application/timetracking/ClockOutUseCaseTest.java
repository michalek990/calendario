package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.NoOpenTimeEntryException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClockOutUseCaseTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_closesOpenEntry() {
        ClockOutUseCase useCase = new ClockOutUseCase(timeEntryRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.findOpenEntryByUserId(1L)).thenReturn(Optional.of(TimeEntry.clockIn(1L)));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase.execute();

        assertThat(view.clockOut()).isNotNull();
    }

    @Test
    void execute_throwsNoOpenEntry_whenNoneIsOpen() {
        ClockOutUseCase useCase = new ClockOutUseCase(timeEntryRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.findOpenEntryByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(useCase::execute).isInstanceOf(NoOpenTimeEntryException.class);
    }
}
