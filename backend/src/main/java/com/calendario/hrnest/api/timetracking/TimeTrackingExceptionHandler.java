package com.calendario.hrnest.api.timetracking;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryActionException;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryManagementException;
import com.calendario.hrnest.domain.timetracking.exception.InvalidTimeEntryRangeException;
import com.calendario.hrnest.domain.timetracking.exception.NoOpenTimeEntryException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyClosedException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyOpenException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TimeTrackingExceptionHandler {

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TimeEntryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenTimeEntryActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenTimeEntryActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenTimeEntryManagementException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenManagement(ForbiddenTimeEntryManagementException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TimeEntryAlreadyOpenException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyOpen(TimeEntryAlreadyOpenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoOpenTimeEntryException.class)
    public ResponseEntity<ErrorResponse> handleNoOpenEntry(NoOpenTimeEntryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TimeEntryAlreadyClosedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyClosed(TimeEntryAlreadyClosedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTimeEntryRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRange(InvalidTimeEntryRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
