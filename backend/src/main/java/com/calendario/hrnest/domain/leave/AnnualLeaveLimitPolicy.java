package com.calendario.hrnest.domain.leave;

import com.calendario.hrnest.domain.leave.exception.AnnualLeaveLimitExceededException;
import java.util.List;
import java.util.Set;

/**
 * Roczny limit urlopu wypoczynkowego (art. 154 Kodeksu Pracy — 26 dni dla stażu > 10 lat).
 * Urlop na żądanie ({@code ON_DEMAND}) prawnie jest częścią tej samej puli
 * wypoczynkowej, więc liczy się do tego samego limitu co {@code VACATION}.
 * Do limitu wliczane są wnioski {@code PENDING} i {@code APPROVED} — wniosek
 * oczekujący też "rezerwuje" pulę, dopóki nie zostanie odrzucony.
 */
public final class AnnualLeaveLimitPolicy {

    public static final int ANNUAL_VACATION_LIMIT_DAYS = 26;

    private static final Set<LeaveType> TYPES_COUNTED_TOWARD_LIMIT = Set.of(LeaveType.VACATION, LeaveType.ON_DEMAND);

    private AnnualLeaveLimitPolicy() {
    }

    public static boolean countsTowardLimit(LeaveType type) {
        return TYPES_COUNTED_TOWARD_LIMIT.contains(type);
    }

    /** Sumuje dni już zajęte (PENDING lub APPROVED) w danym roku kalendarzowym spośród typów objętych limitem. */
    public static long usedDays(List<LeaveRequest> existingRequests, int year) {
        return existingRequests.stream()
                .filter(request -> countsTowardLimit(request.getType()))
                .filter(request -> request.getStatus() == LeaveStatus.PENDING || request.getStatus() == LeaveStatus.APPROVED)
                .filter(request -> request.getStartDate().getYear() == year)
                .mapToLong(LeaveRequest::daysCount)
                .sum();
    }

    /**
     * Sprawdza, czy dodanie {@code candidate} (jeszcze niezapisanego) do
     * {@code existingRequests} zmieściłoby się w rocznym limicie. Rzuca
     * {@link AnnualLeaveLimitExceededException}, jeśli nie.
     */
    public static void ensureWithinLimit(List<LeaveRequest> existingRequests, LeaveRequest candidate) {
        if (!countsTowardLimit(candidate.getType())) {
            return;
        }

        int year = candidate.getStartDate().getYear();
        long alreadyUsed = usedDays(existingRequests, year);
        long requested = candidate.daysCount();

        if (alreadyUsed + requested > ANNUAL_VACATION_LIMIT_DAYS) {
            throw new AnnualLeaveLimitExceededException(year, ANNUAL_VACATION_LIMIT_DAYS, alreadyUsed, requested);
        }
    }
}
