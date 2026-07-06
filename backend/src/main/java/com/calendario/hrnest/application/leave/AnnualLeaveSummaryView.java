package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.domain.leave.LeaveType;
import java.util.Map;

/**
 * Podsumowanie roczne wniosków (tylko status APPROVED) — ile dni pracownik
 * spędził na pracy zdalnej ({@code REMOTE_WORK}) w porównaniu do pozostałych
 * nieobecności, plus wykorzystanie rocznego limitu urlopu wypoczynkowego.
 */
public record AnnualLeaveSummaryView(
        int year,
        Map<LeaveType, Long> daysByType,
        long remoteWorkDays,
        long otherLeaveDays,
        long vacationDaysUsed,
        long vacationDaysRemaining,
        int vacationAnnualLimit
) {
}
