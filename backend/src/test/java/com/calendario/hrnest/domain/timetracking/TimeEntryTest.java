package com.calendario.hrnest.domain.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.timetracking.exception.InvalidTimeEntryRangeException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyClosedException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimeEntryTest {

    @Test
    void clockIn_startsOpenEntry_withNullTotalMinutes() {
        TimeEntry entry = TimeEntry.clockIn(1L);

        assertThat(entry.isOpen()).isTrue();
        assertThat(entry.getClockOut()).isNull();
        assertThat(entry.totalMinutes()).isNull();
        assertThat(entry.getProjectId()).isNull();
    }

    @Test
    void clockIn_withProjectId_assignsProject() {
        TimeEntry entry = TimeEntry.clockIn(1L, 5L);

        assertThat(entry.getProjectId()).isEqualTo(5L);
    }

    @Test
    void clockOut_closesEntry_andComputesTotalMinutes() {
        TimeEntry open = TimeEntry.reconstitute(
                1L, 1L, java.time.Instant.parse("2026-08-03T08:00:00Z"), null, 30, null);

        TimeEntry closed = open.clockOut(java.time.Instant.parse("2026-08-03T16:00:00Z"));

        assertThat(closed.isOpen()).isFalse();
        assertThat(closed.getClockOut()).isNotNull();
    }

    @Test
    void totalMinutes_subtractsBreakMinutes() {
        TimeEntry entry = TimeEntry.reconstitute(
                1L, 1L,
                java.time.Instant.parse("2026-08-03T08:00:00Z"),
                java.time.Instant.parse("2026-08-03T16:00:00Z"),
                30, null);

        assertThat(entry.totalMinutes()).isEqualTo(8 * 60 - 30);
    }

    @Test
    void clockIn_withCustomAt_usesGivenInstant() {
        Instant at = Instant.parse("2026-08-03T09:00:00Z");

        TimeEntry entry = TimeEntry.clockIn(1L, 5L, at);

        assertThat(entry.getClockIn()).isEqualTo(at);
    }

    @Test
    void clockOut_throwsInvalidRange_whenAtNotAfterClockIn() {
        TimeEntry open = TimeEntry.reconstitute(
                1L, 1L, Instant.parse("2026-08-03T08:00:00Z"), null, 0, null);

        assertThatThrownBy(() -> open.clockOut(Instant.parse("2026-08-03T07:00:00Z")))
                .isInstanceOf(InvalidTimeEntryRangeException.class);
    }

    @Test
    void clockOut_throwsAlreadyClosed_whenCalledTwice() {
        TimeEntry closed = TimeEntry.reconstitute(
                1L, 1L,
                java.time.Instant.parse("2026-08-03T08:00:00Z"),
                java.time.Instant.parse("2026-08-03T16:00:00Z"),
                0, null);

        assertThatThrownBy(closed::clockOut).isInstanceOf(TimeEntryAlreadyClosedException.class);
    }

    @Test
    void log_createsClosedEntry_withGivenRange() {
        TimeEntry entry = TimeEntry.log(1L,
                Instant.parse("2026-08-03T08:00:00Z"), Instant.parse("2026-08-03T16:00:00Z"), 30, 7L);

        assertThat(entry.isOpen()).isFalse();
        assertThat(entry.totalMinutes()).isEqualTo(8 * 60 - 30);
        assertThat(entry.getProjectId()).isEqualTo(7L);
    }

    @Test
    void log_throwsInvalidRange_whenEndNotAfterStart() {
        assertThatThrownBy(() -> TimeEntry.log(1L,
                Instant.parse("2026-08-03T16:00:00Z"), Instant.parse("2026-08-03T08:00:00Z"), 0, null))
                .isInstanceOf(InvalidTimeEntryRangeException.class);
    }

    @Test
    void log_throwsInvalidRange_whenEndEqualsStart() {
        Instant same = Instant.parse("2026-08-03T08:00:00Z");

        assertThatThrownBy(() -> TimeEntry.log(1L, same, same, 0, null))
                .isInstanceOf(InvalidTimeEntryRangeException.class);
    }
}
