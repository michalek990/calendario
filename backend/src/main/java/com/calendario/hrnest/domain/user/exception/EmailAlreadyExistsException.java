package com.calendario.hrnest.domain.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Użytkownik z adresem e-mail " + email + " już istnieje");
    }
}
