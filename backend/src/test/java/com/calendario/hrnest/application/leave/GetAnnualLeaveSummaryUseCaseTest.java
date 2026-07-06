package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAnnualLeaveSummaryUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_splitsRemoteWorkFromOtherApprovedDays_forRequestedYear() {
        GetAnnualLeaveSummaryUseCase useCase =
                new GetAnnualLeaveSummaryUseCase(leaveRequestRepository, currentUserProvider);

        LeaveRequest vacation = LeaveRequest.reconstitute(1L, 7L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 14), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest remote = LeaveRequest.reconstitute(2L, 7L, LeaveType.REMOTE_WORK,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest pendingIgnored = LeaveRequest.create(
                7L, LeaveType.VACATION, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10), null);
        LeaveRequest previousYearIgnored = LeaveRequest.reconstitute(3L, 7L, LeaveType.REMOTE_WORK,
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 5), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(leaveRequestRepository.findByRequesterId(7L))
                .thenReturn(List.of(vacation, remote, pendingIgnored, previousYearIgnored));

        AnnualLeaveSummaryView view = useCase.execute(2026);

        assertThat(view.year()).isEqualTo(2026);
        assertThat(view.remoteWorkDays()).isEqualTo(5);
        assertThat(view.otherLeaveDays()).isEqualTo(10);
        assertThat(view.vacationDaysUsed()).isEqualTo(10);
        assertThat(view.vacationDaysRemaining()).isEqualTo(16);
        assertThat(view.vacationAnnualLimit()).isEqualTo(26);
        assertThat(view.daysByType()).containsEntry(LeaveType.VACATION, 10L).containsEntry(LeaveType.REMOTE_WORK, 5L);
    }

    @Test
    void execute_defaultsToCurrentYear_whenYearNotProvided() {
        GetAnnualLeaveSummaryUseCase useCase =
                new GetAnnualLeaveSummaryUseCase(leaveRequestRepository, currentUserProvider);

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(leaveRequestRepository.findByRequesterId(7L)).thenReturn(List.of());

        AnnualLeaveSummaryView view = useCase.execute(null);

        assertThat(view.year()).isEqualTo(LocalDate.now().getYear());
        assertThat(view.remoteWorkDays()).isZero();
        assertThat(view.vacationDaysRemaining()).isEqualTo(26);
    }
}
