package com.calendario.hrnest.domain.leave.exception;

public class ForbiddenLeaveActionException extends RuntimeException {

    public ForbiddenLeaveActionException() {
        super("Tylko przełożony (MANAGER) lub HR (HR_ADMIN) może zatwierdzać lub odrzucać wnioski urlopowe");
    }
}
