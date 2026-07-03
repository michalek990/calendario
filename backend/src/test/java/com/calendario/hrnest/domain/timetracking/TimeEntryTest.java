package com.calendario.hrnest.domain.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyClosedException;
import org.junit.jupiter.api.Test;

class TimeEntryTest {

    @Test
    void clockIn_startsOpenEntry_withNullTotalMinutes() {
        TimeEntry entry = TimeEntry.clockIn(1L);

        assertThat(entry.isOpen()).isTrue();
        assertThat(entry.getClockOut()).isNull();
        assertThat(entry.totalMinutes()).isNull();
    }

    @Test
    void clockOut_closesEntry_andComputesTotalMinutes() {
        TimeEntry open = TimeEntry.reconstitute(
                1L, 1L, java.time.Instant.parse("2026-08-03T08:00:00Z"), null, 30, null);

        TimeEntry closed = open.clockOut();

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
    void clockOut_throwsAlreadyClosed_whenCalledTwice() {
        TimeEntry closed = TimeEntry.reconstitute(
                1L, 1L,
                java.time.Instant.parse("2026-08-03T08:00:00Z"),
                java.time.Instant.parse("2026-08-03T16:00:00Z"),
                0, null);

        assertThatThrownBy(closed::clockOut).isInstanceOf(TimeEntryAlreadyClosedException.class);
    }
}
