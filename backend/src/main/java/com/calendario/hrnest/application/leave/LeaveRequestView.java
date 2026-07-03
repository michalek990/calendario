package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.Instant;
import java.time.LocalDate;

public record LeaveRequestView(
        Long id,
        Long requesterId,
        LeaveType type,
        LocalDate startDate,
        LocalDate endDate,
        long daysCount,
        LeaveStatus status,
        String reason,
        Long approverId,
        Instant approvedAt,
        Instant createdAt
) {

    public static LeaveRequestView from(LeaveRequest leaveRequest) {
        return new LeaveRequestView(
                leaveRequest.getId(),
                leaveRequest.getRequesterId(),
                leaveRequest.getType(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.daysCount(),
                leaveRequest.getStatus(),
                leaveRequest.getReason(),
                leaveRequest.getApproverId(),
                leaveRequest.getApprovedAt(),
                leaveRequest.getCreatedAt());
    }
}
