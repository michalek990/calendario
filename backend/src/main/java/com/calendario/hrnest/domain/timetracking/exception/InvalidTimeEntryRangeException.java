package com.calendario.hrnest.domain.timetracking.exception;

public class InvalidTimeEntryRangeException extends RuntimeException {

    public InvalidTimeEntryRangeException() {
        super("Godzina zakończenia musi być późniejsza niż godzina rozpoczęcia");
    }
}
