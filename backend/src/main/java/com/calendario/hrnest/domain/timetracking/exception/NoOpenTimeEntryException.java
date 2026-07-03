package com.calendario.hrnest.domain.timetracking.exception;

public class NoOpenTimeEntryException extends RuntimeException {

    public NoOpenTimeEntryException() {
        super("Brak otwartego wpisu czasu pracy do zamknięcia — najpierw zrób clock-in");
    }
}
