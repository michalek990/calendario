package com.calendario.hrnest.domain.timetracking.exception;

public class ForbiddenTimeEntryActionException extends RuntimeException {

    public ForbiddenTimeEntryActionException() {
        super("Wpis czasu pracy należy do innego użytkownika");
    }

    public ForbiddenTimeEntryActionException(String message) {
        super(message);
    }
}
