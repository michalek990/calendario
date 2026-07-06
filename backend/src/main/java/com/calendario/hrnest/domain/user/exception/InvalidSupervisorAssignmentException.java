package com.calendario.hrnest.domain.user.exception;

public class InvalidSupervisorAssignmentException extends RuntimeException {

    public InvalidSupervisorAssignmentException() {
        super("Użytkownik nie może być swoim własnym przełożonym");
    }
}
