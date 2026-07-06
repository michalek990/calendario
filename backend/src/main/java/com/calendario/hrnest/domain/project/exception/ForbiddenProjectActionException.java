package com.calendario.hrnest.domain.project.exception;

public class ForbiddenProjectActionException extends RuntimeException {

    public ForbiddenProjectActionException() {
        super("Tylko dział kadr (HR) lub administrator (ADMIN) może tworzyć projekty");
    }
}
