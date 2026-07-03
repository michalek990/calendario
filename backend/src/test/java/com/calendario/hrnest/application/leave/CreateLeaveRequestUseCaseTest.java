package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.LocalDate;
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
}
