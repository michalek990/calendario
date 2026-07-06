package com.calendario.hrnest.application.timetracking;

import java.time.Instant;

public record UpdateTimeEntryCommand(
        Long timeEntryId,
        Instant clockIn,
        Instant clockOut,
        Integer breakMinutes,
        Long projectId
) {
}
