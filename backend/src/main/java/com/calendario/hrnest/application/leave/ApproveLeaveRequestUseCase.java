package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.leave.exception.LeaveRequestNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import org.springframework.stereotype.Component;

@Component
public class ApproveLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ApproveLeaveRequestUseCase(LeaveRequestRepository leaveRequestRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public LeaveRequestView execute(Long leaveRequestId) {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR_ADMIN) {
            throw new ForbiddenLeaveActionException();
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));

        LeaveRequest approved = leaveRequest.approve(currentUserProvider.currentUserId());

        return LeaveRequestView.from(leaveRequestRepository.save(approved));
    }
}
