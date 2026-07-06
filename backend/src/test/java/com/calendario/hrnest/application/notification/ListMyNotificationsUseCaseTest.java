package com.calendario.hrnest.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import com.calendario.hrnest.domain.notification.NotificationType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyNotificationsUseCaseTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_returnsOwnNotifications_mostRecentFirst() {
        ListMyNotificationsUseCase useCase = new ListMyNotificationsUseCase(notificationRepository, currentUserProvider);
        Instant base = Instant.parse("2026-01-01T00:00:00Z");

        Notification older = Notification.reconstitute(1L, 7L, NotificationType.LEAVE_REQUEST_APPROVED, "Stare", 1L,
                false, base);
        Notification newer = Notification.reconstitute(2L, 7L, NotificationType.LEAVE_REQUEST_REJECTED, "Nowe", 2L,
                false, base.plus(1, ChronoUnit.HOURS));

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(notificationRepository.findByRecipientId(7L)).thenReturn(List.of(older, newer));

        List<NotificationView> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).message()).isEqualTo("Nowe");
        assertThat(result.get(1).message()).isEqualTo("Stare");
    }
}
