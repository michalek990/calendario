package com.calendario.hrnest.api.notification;

import com.calendario.hrnest.api.ErrorResponse;
import com.calendario.hrnest.domain.notification.exception.ForbiddenNotificationActionException;
import com.calendario.hrnest.domain.notification.exception.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenNotificationActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenNotificationActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }
}
