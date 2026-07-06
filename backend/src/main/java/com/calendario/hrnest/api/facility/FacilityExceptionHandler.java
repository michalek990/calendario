package com.calendario.hrnest.api.facility;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.facility.exception.DuplicateFacilityNameException;
import com.calendario.hrnest.domain.facility.exception.FacilityInUseException;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FacilityExceptionHandler {

    @ExceptionHandler(FacilityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(FacilityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateFacilityNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(DuplicateFacilityNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(FacilityInUseException.class)
    public ResponseEntity<ErrorResponse> handleInUse(FacilityInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenFacilityActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenFacilityActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }
}
