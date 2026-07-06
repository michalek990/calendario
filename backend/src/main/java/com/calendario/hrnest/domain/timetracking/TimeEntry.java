package com.calendario.hrnest.domain.timetracking;

import com.calendario.hrnest.domain.timetracking.exception.InvalidTimeEntryRangeException;
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
        return clockIn(userId, projectId, null);
    }

    /** Rozpoczyna wpis o podanej chwili ({@code at}), albo o teraz, jeśli {@code at} jest null. */
    public static TimeEntry clockIn(Long userId, Long projectId, Instant at) {
        return new TimeEntry(null, userId, at != null ? at : Instant.now(), null, 0, null, projectId);
    }

    /**
     * Rejestruje już zakończony wpis czasu pracy dla jednego dnia — zamiast "na żywo"
     * clock-in/clock-out, użytkownik podaje wprost godzinę rozpoczęcia i zakończenia.
     */
    public static TimeEntry log(Long userId, Instant clockIn, Instant clockOut, int breakMinutes, Long projectId) {
        if (!clockOut.isAfter(clockIn)) {
            throw new InvalidTimeEntryRangeException();
        }
        return new TimeEntry(null, userId, clockIn, clockOut, breakMinutes, null, projectId);
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
        return clockOut(null);
    }

    /** Zamyka wpis o podanej chwili ({@code at}), albo o teraz, jeśli {@code at} jest null. */
    public TimeEntry clockOut(Instant at) {
        if (clockOut != null) {
            throw new TimeEntryAlreadyClosedException();
        }
        Instant resolvedClockOut = at != null ? at : Instant.now();
        if (!resolvedClockOut.isAfter(clockIn)) {
            throw new InvalidTimeEntryRangeException();
        }
        return new TimeEntry(id, userId, clockIn, resolvedClockOut, breakMinutes, notes, projectId);
    }

    /** Poprawia godziny/przerwę/projekt już zarejestrowanego wpisu — np. gdy clock-in/out zapisał złą godzinę. */
    public TimeEntry update(Instant clockIn, Instant clockOut, int breakMinutes, Long projectId) {
        if (clockOut != null && !clockOut.isAfter(clockIn)) {
            throw new InvalidTimeEntryRangeException();
        }
        return new TimeEntry(id, userId, clockIn, clockOut, breakMinutes, notes, projectId);
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
