package com.calendario.hrnest.api.leave;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.leave.exception.AnnualLeaveLimitExceededException;
import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.leave.exception.IllegalLeaveStateException;
import com.calendario.hrnest.domain.leave.exception.InvalidLeaveRangeException;
import com.calendario.hrnest.domain.leave.exception.LeaveRequestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LeaveExceptionHandler {

    @ExceptionHandler(InvalidLeaveRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRange(InvalidLeaveRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(LeaveRequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalLeaveStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalLeaveStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenLeaveActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenLeaveActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AnnualLeaveLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleAnnualLimitExceeded(AnnualLeaveLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }
}
