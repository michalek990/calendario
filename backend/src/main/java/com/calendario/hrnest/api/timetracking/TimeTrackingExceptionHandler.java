package com.calendario.hrnest.api.timetracking;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.timetracking.exception.NoOpenTimeEntryException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyClosedException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyOpenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TimeTrackingExceptionHandler {

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
}
