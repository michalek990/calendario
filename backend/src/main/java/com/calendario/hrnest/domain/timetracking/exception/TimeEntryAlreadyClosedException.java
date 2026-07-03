package com.calendario.hrnest.domain.timetracking.exception;

public class TimeEntryAlreadyClosedException extends RuntimeException {

    public TimeEntryAlreadyClosedException() {
        super("Ten wpis czasu pracy jest już zamknięty");
    }
}
