package com.calendario.hrnest.domain.user.exception;

public class ForbiddenRoleChangeException extends RuntimeException {

    public ForbiddenRoleChangeException() {
        super("Tylko administrator (ADMIN) może zmieniać role użytkowników");
    }
}
