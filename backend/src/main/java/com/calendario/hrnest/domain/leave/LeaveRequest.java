package com.calendario.hrnest.domain.leave;

import com.calendario.hrnest.domain.leave.exception.IllegalLeaveStateException;
import com.calendario.hrnest.domain.leave.exception.InvalidLeaveRangeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Agregat domenowy — bez adnotacji JPA/Springa, niemutowalny (jak User). */
public final class LeaveRequest {

    private final Long id;
    private final Long requesterId;
    private final LeaveType type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LeaveStatus status;
    private final String reason;
    private final Long approverId;
    private final Instant approvedAt;
    private final Instant createdAt;

    private LeaveRequest(Long id, Long requesterId, LeaveType type, LocalDate startDate, LocalDate endDate,
                          LeaveStatus status, String reason, Long approverId, Instant approvedAt, Instant createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
        this.approverId = approverId;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }

    public static LeaveRequest create(Long requesterId, LeaveType type, LocalDate startDate, LocalDate endDate,
                                       String reason) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidLeaveRangeException();
        }
        return new LeaveRequest(null, requesterId, type, startDate, endDate, LeaveStatus.PENDING, reason,
                null, null, Instant.now());
    }

    public static LeaveRequest reconstitute(Long id, Long requesterId, LeaveType type, LocalDate startDate,
                                             LocalDate endDate, LeaveStatus status, String reason, Long approverId,
                                             Instant approvedAt, Instant createdAt) {
        return new LeaveRequest(id, requesterId, type, startDate, endDate, status, reason, approverId, approvedAt,
                createdAt);
    }

    public LeaveRequest approve(Long approverId) {
        requirePending();
        return new LeaveRequest(id, requesterId, type, startDate, endDate, LeaveStatus.APPROVED, reason,
                approverId, Instant.now(), createdAt);
    }

    public LeaveRequest reject(Long approverId) {
        requirePending();
        return new LeaveRequest(id, requesterId, type, startDate, endDate, LeaveStatus.REJECTED, reason,
                approverId, Instant.now(), createdAt);
    }

    private void requirePending() {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalLeaveStateException(
                    "Wniosek ma status " + status + " — decyzja możliwa tylko dla wniosków PENDING");
        }
    }

    public long daysCount() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public LeaveType getType() {
        return type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Long getApproverId() {
        return approverId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
