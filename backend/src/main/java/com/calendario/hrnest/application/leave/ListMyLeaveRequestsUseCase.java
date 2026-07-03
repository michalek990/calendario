package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListMyLeaveRequestsUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListMyLeaveRequestsUseCase(LeaveRequestRepository leaveRequestRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<LeaveRequestView> execute() {
        return leaveRequestRepository.findByRequesterId(currentUserProvider.currentUserId()).stream()
                .map(LeaveRequestView::from)
                .toList();
    }
}
