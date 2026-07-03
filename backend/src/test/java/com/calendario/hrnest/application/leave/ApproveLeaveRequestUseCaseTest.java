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
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.leave.exception.LeaveRequestNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApproveLeaveRequestUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_approvesRequest_whenCallerIsManager() {
        ApproveLeaveRequestUseCase useCase = new ApproveLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        LeaveRequest pending = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestView view = useCase.execute(1L);

        assertThat(view.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(view.approverId()).isEqualTo(99L);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        ApproveLeaveRequestUseCase useCase = new ApproveLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(ForbiddenLeaveActionException.class);
    }

    @Test
    void execute_throwsNotFound_whenRequestDoesNotExist() {
        ApproveLeaveRequestUseCase useCase = new ApproveLeaveRequestUseCase(leaveRequestRepository, currentUserProvider);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR_ADMIN);
        when(leaveRequestRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(404L)).isInstanceOf(LeaveRequestNotFoundException.class);
    }
}
