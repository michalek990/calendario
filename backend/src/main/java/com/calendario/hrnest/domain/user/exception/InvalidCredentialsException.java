package com.calendario.hrnest.domain.user.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Nieprawidłowy e-mail lub hasło");
    }
}
