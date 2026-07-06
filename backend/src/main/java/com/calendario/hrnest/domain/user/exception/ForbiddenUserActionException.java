package com.calendario.hrnest.domain.user.exception;

public class ForbiddenUserActionException extends RuntimeException {

    public ForbiddenUserActionException() {
        super("Tylko dział kadr (HR) lub administrator (ADMIN) może edytować dane organizacyjne użytkownika");
    }
}
