package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(NotificationRepositoryAdapter.class)
class NotificationRepositoryAdapterTest {

    @Autowired
    private NotificationRepositoryAdapter adapter;

    @Test
    void save_thenFindById_roundTripsAllFields() {
        Notification notification = Notification.create(7L, NotificationType.LEAVE_REQUEST_APPROVED, "Tresc", 3L);

        Notification saved = adapter.save(notification);

        assertThat(saved.getId()).isNotNull();

        Notification found = adapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getRecipientId()).isEqualTo(7L);
        assertThat(found.getType()).isEqualTo(NotificationType.LEAVE_REQUEST_APPROVED);
        assertThat(found.getMessage()).isEqualTo("Tresc");
        assertThat(found.getLeaveRequestId()).isEqualTo(3L);
        assertThat(found.isRead()).isFalse();
    }

    @Test
    void findByRecipientId_returnsOnlyThatRecipientsNotifications() {
        adapter.save(Notification.create(7L, NotificationType.LEAVE_REQUEST_APPROVED, "A", 1L));
        adapter.save(Notification.create(8L, NotificationType.LEAVE_REQUEST_REJECTED, "B", 2L));

        assertThat(adapter.findByRecipientId(7L)).hasSize(1);
        assertThat(adapter.findByRecipientId(7L).get(0).getRecipientId()).isEqualTo(7L);
    }

    @Test
    void save_afterMarkAsRead_persistsReadFlag() {
        Notification saved = adapter.save(Notification.create(7L, NotificationType.LEAVE_REQUEST_APPROVED, "A", 1L));

        adapter.save(saved.markAsRead());

        Notification found = adapter.findById(saved.getId()).orElseThrow();
        assertThat(found.isRead()).isTrue();
    }
}
