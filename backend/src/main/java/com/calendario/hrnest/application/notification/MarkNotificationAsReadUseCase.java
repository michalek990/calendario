package com.calendario.hrnest.application.notification;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import com.calendario.hrnest.domain.notification.exception.ForbiddenNotificationActionException;
import com.calendario.hrnest.domain.notification.exception.NotificationNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    public MarkNotificationAsReadUseCase(NotificationRepository notificationRepository,
                                          CurrentUserProvider currentUserProvider) {
        this.notificationRepository = notificationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public NotificationView execute(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getRecipientId().equals(currentUserProvider.currentUserId())) {
            throw new ForbiddenNotificationActionException();
        }

        return NotificationView.from(notificationRepository.save(notification.markAsRead()));
    }
}
