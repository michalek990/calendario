package com.calendario.hrnest.domain.user.exception;

public class ForbiddenUserActionException extends RuntimeException {

    public ForbiddenUserActionException() {
        super("Tylko HR (HR_ADMIN) może edytować dane organizacyjne użytkownika");
    }
}
