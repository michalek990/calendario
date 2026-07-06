package com.calendario.hrnest.domain.notification.exception;

public class ForbiddenNotificationActionException extends RuntimeException {

    public ForbiddenNotificationActionException() {
        super("Powiadomienie należy do innego użytkownika");
    }
}
