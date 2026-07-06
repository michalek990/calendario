package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.domain.timetracking.TimeEntry;
import java.time.Instant;

public record TimeEntryView(
        Long id,
        Long userId,
        Instant clockIn,
        Instant clockOut,
        int breakMinutes,
        Long totalMinutes,
        String notes,
        Long projectId
) {

    public static TimeEntryView from(TimeEntry timeEntry) {
        return new TimeEntryView(
                timeEntry.getId(),
                timeEntry.getUserId(),
                timeEntry.getClockIn(),
                timeEntry.getClockOut(),
                timeEntry.getBreakMinutes(),
                timeEntry.totalMinutes(),
                timeEntry.getNotes(),
                timeEntry.getProjectId());
    }
}
