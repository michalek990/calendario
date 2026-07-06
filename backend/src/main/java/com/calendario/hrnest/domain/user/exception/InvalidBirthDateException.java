package com.calendario.hrnest.domain.user.exception;

public class InvalidBirthDateException extends RuntimeException {

    public InvalidBirthDateException() {
        super("Data urodzenia nie może być w przyszłości");
    }
}
