package com.calendario.hrnest.domain.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.leave.exception.AnnualLeaveLimitExceededException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnnualLeaveLimitPolicyTest {

    @Test
    void countsTowardLimit_trueOnlyForVacationAndOnDemand() {
        assertThat(AnnualLeaveLimitPolicy.countsTowardLimit(LeaveType.VACATION)).isTrue();
        assertThat(AnnualLeaveLimitPolicy.countsTowardLimit(LeaveType.ON_DEMAND)).isTrue();
        assertThat(AnnualLeaveLimitPolicy.countsTowardLimit(LeaveType.REMOTE_WORK)).isFalse();
        assertThat(AnnualLeaveLimitPolicy.countsTowardLimit(LeaveType.SICK_LEAVE)).isFalse();
    }

    @Test
    void ensureWithinLimit_allows_whenUnderLimit() {
        LeaveRequest existing = LeaveRequest.reconstitute(1L, 1L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 14), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest candidate = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null);

        AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(existing), candidate);
    }

    @Test
    void ensureWithinLimit_throws_whenExceedingLimit() {
        LeaveRequest existing = LeaveRequest.reconstitute(1L, 1L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest candidate = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null);

        assertThatThrownBy(() -> AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(existing), candidate))
                .isInstanceOf(AnnualLeaveLimitExceededException.class);
    }

    @Test
    void ensureWithinLimit_countsOnDemandAndVacationTogether() {
        LeaveRequest vacation = LeaveRequest.reconstitute(1L, 1L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest onDemandCandidate = LeaveRequest.create(
                1L, LeaveType.ON_DEMAND, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null);

        assertThatThrownBy(() -> AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(vacation), onDemandCandidate))
                .isInstanceOf(AnnualLeaveLimitExceededException.class);
    }

    @Test
    void ensureWithinLimit_ignoresRejectedAndCancelledRequests() {
        LeaveRequest rejected = LeaveRequest.reconstitute(1L, 1L, LeaveType.VACATION,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), LeaveStatus.REJECTED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest candidate = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null);

        AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(rejected), candidate);
    }

    @Test
    void ensureWithinLimit_ignoresRequestsFromDifferentYear() {
        LeaveRequest previousYear = LeaveRequest.reconstitute(1L, 1L, LeaveType.VACATION,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 20), LeaveStatus.APPROVED, null, 9L,
                Instant.now(), Instant.now());
        LeaveRequest candidate = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), null);

        AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(previousYear), candidate);
    }

    @Test
    void ensureWithinLimit_ignoresTypesNotCountedTowardLimit() {
        LeaveRequest candidate = LeaveRequest.create(
                1L, LeaveType.REMOTE_WORK, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        AnnualLeaveLimitPolicy.ensureWithinLimit(List.of(), candidate);
    }
}
