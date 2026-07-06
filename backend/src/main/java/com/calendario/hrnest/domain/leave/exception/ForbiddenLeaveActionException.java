package com.calendario.hrnest.domain.leave.exception;

public class ForbiddenLeaveActionException extends RuntimeException {

    public ForbiddenLeaveActionException() {
        super("Tylko bezpośredni przełożony (MANAGER), dział kadr (HR) lub administrator (ADMIN) "
                + "może zatwierdzać lub odrzucać wnioski urlopowe");
    }
}
