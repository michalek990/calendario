package com.calendario.hrnest.domain.facility.exception;

/** Rzucany przy próbie usunięcia zakładu, do którego wciąż przypisani są pracownicy. */
public class FacilityInUseException extends RuntimeException {

    public FacilityInUseException(String name) {
        super("Nie można usunąć zakładu \"" + name + "\" — są do niego przypisani pracownicy");
    }
}
