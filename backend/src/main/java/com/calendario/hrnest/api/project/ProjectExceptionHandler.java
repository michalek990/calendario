package com.calendario.hrnest.api.project;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.project.exception.DuplicateProjectNameException;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectActionException;
import com.calendario.hrnest.domain.project.exception.ForbiddenProjectSummaryAccessException;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(DuplicateProjectNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenProjectActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenProjectActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenProjectSummaryAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenSummaryAccess(ForbiddenProjectSummaryAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }
}
