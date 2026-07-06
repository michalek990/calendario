package com.calendario.hrnest.api.timetracking;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateTimeEntryRequest(
        @NotNull Instant clockIn,
        Instant clockOut,
        Integer breakMinutes,
        Long projectId
) {
}
