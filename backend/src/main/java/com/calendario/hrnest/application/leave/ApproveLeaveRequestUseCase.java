package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.notification.LeaveDecisionNotifier;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.leave.exception.LeaveRequestNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ApproveLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final LeaveDecisionNotifier leaveDecisionNotifier;

    public ApproveLeaveRequestUseCase(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository,
                                       CurrentUserProvider currentUserProvider,
                                       LeaveDecisionNotifier leaveDecisionNotifier) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.leaveDecisionNotifier = leaveDecisionNotifier;
    }

    public LeaveRequestView execute(Long leaveRequestId) {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenLeaveActionException();
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        if (role == Role.MANAGER) {
            LeaveRequestScopeGuard.requireDirectSupervisorOf(
                    userRepository, currentUserProvider.currentUserId(), leaveRequest.getRequesterId());
        }

        LeaveRequest approved = leaveRequest.approve(currentUserProvider.currentUserId());
        LeaveRequest saved = leaveRequestRepository.save(approved);

        leaveDecisionNotifier.notifyDecision(saved);

        return LeaveRequestView.from(saved);
    }
}
