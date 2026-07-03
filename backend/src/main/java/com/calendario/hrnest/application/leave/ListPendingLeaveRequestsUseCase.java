package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.user.Role;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListPendingLeaveRequestsUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListPendingLeaveRequestsUseCase(LeaveRequestRepository leaveRequestRepository,
                                            CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<LeaveRequestView> execute() {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR_ADMIN) {
            throw new ForbiddenLeaveActionException();
        }

        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING).stream()
                .map(LeaveRequestView::from)
                .toList();
    }
}
