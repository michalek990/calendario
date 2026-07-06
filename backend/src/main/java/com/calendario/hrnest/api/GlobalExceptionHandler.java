package com.calendario.hrnest.api;

import com.calendario.hrnest.domain.user.exception.EmailAlreadyExistsException;
import com.calendario.hrnest.domain.user.exception.ForbiddenRoleChangeException;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import com.calendario.hrnest.domain.user.exception.InvalidBirthDateException;
import com.calendario.hrnest.domain.user.exception.InvalidCredentialsException;
import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Centralne mapowanie wyjątków domenowych na odpowiedzi HTTP — kontrolery i use case'y ich nie znają. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenUserActionException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenUserAction(ForbiddenUserActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidSupervisorAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSupervisorAssignment(InvalidSupervisorAssignmentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidBirthDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBirthDate(InvalidBirthDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenRoleChangeException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenRoleChange(ForbiddenRoleChangeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }
}
