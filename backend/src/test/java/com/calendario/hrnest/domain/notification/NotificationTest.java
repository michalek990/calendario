package com.calendario.hrnest.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void create_startsAsUnread() {
        Notification notification = Notification.create(1L, NotificationType.LEAVE_REQUEST_APPROVED, "Tresc", 5L);

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getRecipientId()).isEqualTo(1L);
        assertThat(notification.getLeaveRequestId()).isEqualTo(5L);
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    void markAsRead_setsReadTrue_keepsOtherFields() {
        Notification notification = Notification.create(1L, NotificationType.LEAVE_REQUEST_REJECTED, "Tresc", 5L);

        Notification read = notification.markAsRead();

        assertThat(read.isRead()).isTrue();
        assertThat(read.getRecipientId()).isEqualTo(1L);
        assertThat(read.getMessage()).isEqualTo("Tresc");
    }
}
