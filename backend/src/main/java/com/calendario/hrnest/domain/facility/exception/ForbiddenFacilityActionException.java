package com.calendario.hrnest.domain.facility.exception;

public class ForbiddenFacilityActionException extends RuntimeException {

    public ForbiddenFacilityActionException() {
        super("Tylko administrator (ADMIN) może zarządzać zakładami");
    }
}
