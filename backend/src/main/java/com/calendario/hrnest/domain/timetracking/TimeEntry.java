package com.calendario.hrnest.domain.timetracking;

import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyClosedException;
import java.time.Duration;
import java.time.Instant;

/** Agregat domenowy — bez adnotacji JPA/Springa, niemutowalny (jak User/LeaveRequest). */
public final class TimeEntry {

    private final Long id;
    private final Long userId;
    private final Instant clockIn;
    private final Instant clockOut;
    private final int breakMinutes;
    private final String notes;
    private final Long projectId;

    private TimeEntry(Long id, Long userId, Instant clockIn, Instant clockOut, int breakMinutes, String notes,
                       Long projectId) {
        this.id = id;
        this.userId = userId;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.breakMinutes = breakMinutes;
        this.notes = notes;
        this.projectId = projectId;
    }

    public static TimeEntry clockIn(Long userId) {
        return clockIn(userId, null);
    }

    /** Rozpoczyna wpis, opcjonalnie przypisany do konkretnego projektu ({@code projectId} może być null). */
    public static TimeEntry clockIn(Long userId, Long projectId) {
        return new TimeEntry(null, userId, Instant.now(), null, 0, null, projectId);
    }

    public static TimeEntry reconstitute(Long id, Long userId, Instant clockIn, Instant clockOut,
                                          int breakMinutes, String notes) {
        return reconstitute(id, userId, clockIn, clockOut, breakMinutes, notes, null);
    }

    public static TimeEntry reconstitute(Long id, Long userId, Instant clockIn, Instant clockOut,
                                          int breakMinutes, String notes, Long projectId) {
        return new TimeEntry(id, userId, clockIn, clockOut, breakMinutes, notes, projectId);
    }

    public TimeEntry clockOut() {
        if (clockOut != null) {
            throw new TimeEntryAlreadyClosedException();
        }
        return new TimeEntry(id, userId, clockIn, Instant.now(), breakMinutes, notes, projectId);
    }

    /** Minuty przepracowane (clockIn -> clockOut minus przerwa), albo null dopóki wpis jest otwarty. */
    public Long totalMinutes() {
        if (clockOut == null) {
            return null;
        }
        return Duration.between(clockIn, clockOut).toMinutes() - breakMinutes;
    }

    public boolean isOpen() {
        return clockOut == null;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getClockIn() {
        return clockIn;
    }

    public Instant getClockOut() {
        return clockOut;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public Long getProjectId() {
        return projectId;
    }
}
