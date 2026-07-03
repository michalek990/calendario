package com.calendario.hrnest.domain.timetracking.exception;

public class TimeEntryAlreadyOpenException extends RuntimeException {

    public TimeEntryAlreadyOpenException() {
        super("Masz już otwarty wpis czasu pracy — najpierw zrób clock-out");
    }
}
