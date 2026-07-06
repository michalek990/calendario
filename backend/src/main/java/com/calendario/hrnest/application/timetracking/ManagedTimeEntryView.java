package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.user.User;
import java.time.Instant;

/** Wpis czasu pracy wzbogacony o dane pracownika — do widoku zarządzania zespołem (HR/MANAGER/ADMIN). */
public record ManagedTimeEntryView(
        Long id,
        Long userId,
        String userFirstName,
        String userLastName,
        String userEmail,
        Instant clockIn,
        Instant clockOut,
        int breakMinutes,
        Long totalMinutes,
        String notes,
        Long projectId
) {

    public static ManagedTimeEntryView from(TimeEntry timeEntry, User user) {
        return new ManagedTimeEntryView(
                timeEntry.getId(),
                timeEntry.getUserId(),
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName() : null,
                user != null ? user.getEmail() : null,
                timeEntry.getClockIn(),
                timeEntry.getClockOut(),
                timeEntry.getBreakMinutes(),
                timeEntry.totalMinutes(),
                timeEntry.getNotes(),
                timeEntry.getProjectId());
    }
}
