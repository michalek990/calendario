package com.calendario.hrnest.domain.leave.exception;

public class InvalidLeaveRangeException extends RuntimeException {

    public InvalidLeaveRangeException() {
        super("Data zakończenia urlopu nie może być wcześniejsza niż data rozpoczęcia");
    }
}
