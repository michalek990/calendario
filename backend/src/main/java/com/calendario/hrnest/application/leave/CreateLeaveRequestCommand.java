package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.LocalDate;

public record CreateLeaveRequestCommand(LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
}
