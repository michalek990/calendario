package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import com.calendario.hrnest.domain.leave.exception.AnnualLeaveLimitExceededException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateLeaveRequestUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_createsRequestForCurrentUser() {
        CreateLeaveRequestUseCase useCase = new CreateLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestView view = useCase.execute(new CreateLeaveRequestCommand(
                LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 5), "Wyjazd"));

        assertThat(view.requesterId()).isEqualTo(7L);
        assertThat(view.daysCount()).isEqualTo(3);
    }

    @Test
    void execute_throwsAnnualLeaveLimitExceeded_whenOverAnnualVacationCap() {
        CreateLeaveRequestUseCase useCase = new CreateLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(7L);

        LeaveRequest alreadyApproved = LeaveRequest.reconstitute(1L, 7L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        when(leaveRequestRepository.findByRequesterId(7L)).thenReturn(List.of(alreadyApproved));

        assertThatThrownBy(() -> useCase.execute(new CreateLeaveRequestCommand(
                LeaveType.VACATION, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null)))
                .isInstanceOf(AnnualLeaveLimitExceededException.class);
    }

    @Test
    void execute_doesNotCheckAnnualLimit_forTypesNotCoveredByIt() {
        CreateLeaveRequestUseCase useCase = new CreateLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest alreadyApproved = LeaveRequest.reconstitute(1L, 7L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        when(leaveRequestRepository.findByRequesterId(7L)).thenReturn(List.of(alreadyApproved));

        LeaveRequestView view = useCase.execute(new CreateLeaveRequestCommand(
                LeaveType.REMOTE_WORK, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null));

        assertThat(view.type()).isEqualTo(LeaveType.REMOTE_WORK);
    }
}
