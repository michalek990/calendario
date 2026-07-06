package com.calendario.hrnest.domain.user.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("Nie znaleziono użytkownika o id " + userId);
    }
}
