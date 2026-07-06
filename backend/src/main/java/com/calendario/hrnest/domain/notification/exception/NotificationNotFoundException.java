package com.calendario.hrnest.domain.notification.exception;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long notificationId) {
        super("Nie znaleziono powiadomienia o id " + notificationId);
    }
}
