package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import org.springframework.stereotype.Component;

@Component
public class CreateLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public CreateLeaveRequestUseCase(LeaveRequestRepository leaveRequestRepository,
                                      CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public LeaveRequestView execute(CreateLeaveRequestCommand command) {
        LeaveRequest leaveRequest = LeaveRequest.create(
                currentUserProvider.currentUserId(),
                command.type(),
                command.startDate(),
                command.endDate(),
                command.reason());

        return LeaveRequestView.from(leaveRequestRepository.save(leaveRequest));
    }
}
