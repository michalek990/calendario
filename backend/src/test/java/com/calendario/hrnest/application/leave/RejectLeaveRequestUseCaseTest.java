package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.notification.LeaveDecisionNotifier;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RejectLeaveRequestUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private LeaveDecisionNotifier leaveDecisionNotifier;

    private RejectLeaveRequestUseCase useCase() {
        return new RejectLeaveRequestUseCase(leaveRequestRepository, userRepository, currentUserProvider,
                leaveDecisionNotifier);
    }

    @Test
    void execute_rejectsRequest_whenCallerIsHr() {
        LeaveRequest pending = LeaveRequest.create(
                1L, LeaveType.OTHER, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR, Instant.now());
        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(5L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestView view = useCase().execute(1L);

        assertThat(view.status()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveDecisionNotifier).notifyDecision(any(LeaveRequest.class));
    }

    @Test
    void execute_throwsForbidden_whenHrIsOutsideRequesterFacility() {
        LeaveRequest pending = LeaveRequest.create(
                1L, LeaveType.OTHER, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                null, null, "Krakow", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(5L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(ForbiddenLeaveActionException.class);
    }

    @Test
    void execute_rejectsRequest_whenCallerIsDirectSupervisor() {
        LeaveRequest pending = LeaveRequest.create(
                1L, LeaveType.OTHER, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now()).updateOrganization(null, null, null, 99L);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(useCase().execute(1L).status()).isEqualTo(LeaveStatus.REJECTED);
    }

    @Test
    void execute_throwsForbidden_whenManagerIsNotDirectSupervisorOfRequester() {
        LeaveRequest pending = LeaveRequest.create(
                1L, LeaveType.OTHER, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now()).updateOrganization(null, null, null, 7L);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(ForbiddenLeaveActionException.class);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(ForbiddenLeaveActionException.class);
    }
}
