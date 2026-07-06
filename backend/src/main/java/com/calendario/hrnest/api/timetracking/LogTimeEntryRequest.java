package com.calendario.hrnest.api.timetracking;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record LogTimeEntryRequest(
        @NotNull Instant clockIn,
        @NotNull Instant clockOut,
        Integer breakMinutes,
        Long projectId
) {
}
