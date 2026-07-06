package com.calendario.hrnest.domain.facility.exception;

public class DuplicateFacilityNameException extends RuntimeException {

    public DuplicateFacilityNameException(String name) {
        super("Zakład o nazwie \"" + name + "\" już istnieje");
    }
}
