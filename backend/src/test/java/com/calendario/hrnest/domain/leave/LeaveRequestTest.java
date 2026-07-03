package com.calendario.hrnest.domain.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.leave.exception.IllegalLeaveStateException;
import com.calendario.hrnest.domain.leave.exception.InvalidLeaveRangeException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LeaveRequestTest {

    @Test
    void create_startsAsPending_withComputedDaysCount() {
        LeaveRequest request = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7), "Wakacje");

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(request.daysCount()).isEqualTo(5);
        assertThat(request.getApproverId()).isNull();
    }

    @Test
    void create_throwsInvalidLeaveRange_whenEndBeforeStart() {
        assertThatThrownBy(() -> LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 3), null))
                .isInstanceOf(InvalidLeaveRangeException.class);
    }

    @Test
    void approve_setsApprovedStatusAndApprover() {
        LeaveRequest request = LeaveRequest.create(
                1L, LeaveType.SICK_LEAVE, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);

        LeaveRequest approved = request.approve(99L);

        assertThat(approved.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(approved.getApproverId()).isEqualTo(99L);
        assertThat(approved.getApprovedAt()).isNotNull();
    }

    @Test
    void reject_setsRejectedStatusAndApprover() {
        LeaveRequest request = LeaveRequest.create(
                1L, LeaveType.UNPAID, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);

        LeaveRequest rejected = request.reject(99L);

        assertThat(rejected.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(rejected.getApproverId()).isEqualTo(99L);
    }

    @Test
    void approve_throwsIllegalLeaveState_whenAlreadyDecided() {
        LeaveRequest request = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null);
        LeaveRequest approved = request.approve(99L);

        assertThatThrownBy(() -> approved.reject(99L)).isInstanceOf(IllegalLeaveStateException.class);
    }
}
