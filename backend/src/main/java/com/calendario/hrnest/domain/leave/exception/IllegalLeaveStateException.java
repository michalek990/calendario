package com.calendario.hrnest.domain.leave.exception;

public class IllegalLeaveStateException extends RuntimeException {

    public IllegalLeaveStateException(String message) {
        super(message);
    }
}
