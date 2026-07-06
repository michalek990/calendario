package com.calendario.hrnest.application.notification;

import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationType;
import java.time.Instant;

public record NotificationView(
        Long id,
        NotificationType type,
        String message,
        Long leaveRequestId,
        boolean read,
        Instant createdAt
) {

    public static NotificationView from(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getLeaveRequestId(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
