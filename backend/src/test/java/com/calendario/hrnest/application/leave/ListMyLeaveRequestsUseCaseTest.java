package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyLeaveRequestsUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ListMyLeaveRequestsUseCase useCase() {
        return new ListMyLeaveRequestsUseCase(leaveRequestRepository, currentUserProvider);
    }

    @Test
    void execute_returnsOnlyRequestsOfCurrentUser() {
        LeaveRequest ownRequest = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 4), "Wakacje");

        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(leaveRequestRepository.findByRequesterId(1L)).thenReturn(List.of(ownRequest));

        List<LeaveRequestView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).requesterId()).isEqualTo(1L);
        assertThat(result.get(0).reason()).isEqualTo("Wakacje");
    }

    @Test
    void execute_returnsEmptyList_whenUserHasNoRequests() {
        when(currentUserProvider.currentUserId()).thenReturn(2L);
        when(leaveRequestRepository.findByRequesterId(2L)).thenReturn(List.of());

        assertThat(useCase().execute()).isEmpty();
    }
}
