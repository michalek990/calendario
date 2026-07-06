package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.common.FacilityScope;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListPendingLeaveRequestsUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListPendingLeaveRequestsUseCase(LeaveRequestRepository leaveRequestRepository,
                                            UserRepository userRepository,
                                            CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<LeaveRequestView> execute() {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenLeaveActionException();
        }

        var pending = leaveRequestRepository.findByStatus(LeaveStatus.PENDING).stream();

        if (role == Role.MANAGER) {
            Long managerId = currentUserProvider.currentUserId();
            pending = pending.filter(leaveRequest ->
                    LeaveRequestScopeGuard.isDirectSupervisor(userRepository, managerId, leaveRequest.getRequesterId()));
        } else if (role == Role.HR) {
            Long hrId = currentUserProvider.currentUserId();
            pending = pending.filter(leaveRequest -> FacilityScope.isSameFacility(
                    userRepository, hrId, leaveRequest.getRequesterId()));
        }

        return pending.map(LeaveRequestView::from).toList();
    }
}
