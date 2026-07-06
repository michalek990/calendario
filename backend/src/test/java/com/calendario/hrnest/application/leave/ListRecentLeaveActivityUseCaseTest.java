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
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRecentLeaveActivityUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_returnsRequests_mostRecentlyChangedFirst() {
        ListRecentLeaveActivityUseCase useCase =
                new ListRecentLeaveActivityUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(7L);

        Instant base = Instant.parse("2026-01-01T00:00:00Z");

        // createdAt najstarszy, bez decyzji -> ostatnia zmiana to createdAt.
        LeaveRequest older = LeaveRequest.reconstitute(1L, 7L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 5), LeaveStatus.PENDING, "Stary",
                null, null, base);

        // createdAt świeższy niż `older`, wciąż bez decyzji.
        LeaveRequest newer = LeaveRequest.reconstitute(2L, 7L, LeaveType.REMOTE_WORK,
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), LeaveStatus.PENDING, "Praca z domu",
                null, null, base.plus(1, ChronoUnit.HOURS));

        // createdAt najstarszy ze wszystkich, ale zatwierdzony najpóźniej -> to jest ostatnia zmiana.
        LeaveRequest decided = LeaveRequest.reconstitute(3L, 7L, LeaveType.ON_DEMAND,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), LeaveStatus.APPROVED, "Na żądanie",
                1L, base.plus(2, ChronoUnit.HOURS), base.minus(10, ChronoUnit.DAYS));

        when(leaveRequestRepository.findByRequesterId(7L)).thenReturn(List.of(older, newer, decided));

        List<LeaveRequestView> result = useCase.execute();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).type()).isEqualTo(LeaveType.ON_DEMAND);
        assertThat(result.get(1).type()).isEqualTo(LeaveType.REMOTE_WORK);
        assertThat(result.get(2).type()).isEqualTo(LeaveType.VACATION);
    }

    @Test
    void execute_limitsToTenMostRecent() {
        ListRecentLeaveActivityUseCase useCase =
                new ListRecentLeaveActivityUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserId()).thenReturn(7L);

        List<LeaveRequest> requests = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> LeaveRequest.reconstitute((long) i, 7L, LeaveType.VACATION,
                        LocalDate.of(2026, 1, 1).plusDays(i), LocalDate.of(2026, 1, 1).plusDays(i),
                        LeaveStatus.PENDING, null, null, null,
                        Instant.parse("2026-01-01T00:00:00Z").plus(i, ChronoUnit.HOURS)))
                .toList();
        when(leaveRequestRepository.findByRequesterId(7L)).thenReturn(requests);

        assertThat(useCase.execute()).hasSize(10);
    }
}
