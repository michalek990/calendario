package com.calendario.hrnest.application.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListPendingLeaveRequestsUseCaseTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ListPendingLeaveRequestsUseCase useCase() {
        return new ListPendingLeaveRequestsUseCase(leaveRequestRepository, userRepository, currentUserProvider);
    }

    @Test
    void execute_asHr_returnsAllPendingRequests_forOwnFacility() {
        LeaveRequest requestA = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        LeaveRequest requestB = LeaveRequest.create(
                2L, LeaveType.VACATION, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 4), null);
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR, Instant.now());
        User requesterA = User.reconstitute(1L, "a@example.com", "hash", "A", "A", Role.EMPLOYEE, Instant.now());
        User requesterB = User.reconstitute(2L, "b@example.com", "hash", "B", "B", Role.EMPLOYEE, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(List.of(requestA, requestB));
        when(userRepository.findById(5L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requesterA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(requesterB));

        assertThat(useCase().execute()).hasSize(2);
    }

    @Test
    void execute_asHr_onlyReturnsRequestsFromOwnFacility() {
        LeaveRequest sameFacilityRequest = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        LeaveRequest otherFacilityRequest = LeaveRequest.create(
                2L, LeaveType.VACATION, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 4), null);
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User sameFacilityRequester = User.reconstitute(1L, "a@example.com", "hash", "A", "A", Role.EMPLOYEE,
                null, null, "Warszawa", null, Instant.now());
        User otherFacilityRequester = User.reconstitute(2L, "b@example.com", "hash", "B", "B", Role.EMPLOYEE,
                null, null, "Krakow", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(List.of(sameFacilityRequest, otherFacilityRequest));
        when(userRepository.findById(5L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sameFacilityRequester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherFacilityRequester));

        List<LeaveRequestView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).requesterId()).isEqualTo(1L);
    }

    @Test
    void execute_asManager_onlyReturnsDirectReports() {
        LeaveRequest reporteeRequest = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        LeaveRequest strangerRequest = LeaveRequest.create(
                2L, LeaveType.VACATION, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 4), null);

        User reportee = User.reconstitute(1L, "a@example.com", "hash", "A", "A", Role.EMPLOYEE, Instant.now())
                .updateOrganization(null, null, null, 99L);
        User stranger = User.reconstitute(2L, "b@example.com", "hash", "B", "B", Role.EMPLOYEE, Instant.now())
                .updateOrganization(null, null, null, 7L);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(leaveRequestRepository.findByStatus(LeaveStatus.PENDING))
                .thenReturn(List.of(reporteeRequest, strangerRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(reportee));
        when(userRepository.findById(2L)).thenReturn(Optional.of(stranger));

        List<LeaveRequestView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).requesterId()).isEqualTo(1L);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute()).isInstanceOf(ForbiddenLeaveActionException.class);
    }
}
