package com.calendario.hrnest.domain.facility.exception;

public class FacilityNotFoundException extends RuntimeException {

    public FacilityNotFoundException(Long facilityId) {
        super("Nie znaleziono zakładu o id " + facilityId);
    }

    public FacilityNotFoundException(String name) {
        super("Nie znaleziono zakładu o nazwie \"" + name + "\"");
    }
}
