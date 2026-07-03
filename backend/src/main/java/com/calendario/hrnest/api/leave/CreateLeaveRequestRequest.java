package com.calendario.hrnest.api.leave;

import com.calendario.hrnest.domain.leave.LeaveType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateLeaveRequestRequest(
        @NotNull LeaveType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {
}
