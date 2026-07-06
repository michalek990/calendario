package com.calendario.hrnest.domain.leave.exception;

public class AnnualLeaveLimitExceededException extends RuntimeException {

    public AnnualLeaveLimitExceededException(int year, int limitDays, long alreadyUsedDays, long requestedDays) {
        super("Roczny limit urlopu wypoczynkowego na rok " + year + " wynosi " + limitDays
                + " dni. Wykorzystano już (lub zarezerwowano oczekującymi wnioskami) " + alreadyUsedDays
                + " dni, a ten wniosek dodałby kolejne " + requestedDays + " dni.");
    }
}
