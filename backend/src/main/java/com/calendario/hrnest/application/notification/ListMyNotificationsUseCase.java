package com.calendario.hrnest.application.notification;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListMyNotificationsUseCase {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListMyNotificationsUseCase(NotificationRepository notificationRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.notificationRepository = notificationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<NotificationView> execute() {
        return notificationRepository.findByRecipientId(currentUserProvider.currentUserId()).stream()
                .map(NotificationView::from)
                .sorted(Comparator.comparing(NotificationView::createdAt).reversed())
                .toList();
    }
}
