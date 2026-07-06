package com.calendario.hrnest.domain.timetracking.exception;

public class TimeEntryNotFoundException extends RuntimeException {

    public TimeEntryNotFoundException(Long timeEntryId) {
        super("Nie znaleziono wpisu czasu pracy o id " + timeEntryId);
    }
}
