package com.calendario.hrnest.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import com.calendario.hrnest.domain.notification.NotificationType;
import com.calendario.hrnest.domain.notification.exception.ForbiddenNotificationActionException;
import com.calendario.hrnest.domain.notification.exception.NotificationNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadUseCaseTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_marksOwnNotificationAsRead() {
        MarkNotificationAsReadUseCase useCase =
                new MarkNotificationAsReadUseCase(notificationRepository, currentUserProvider);
        Notification notification = Notification.create(7L, NotificationType.LEAVE_REQUEST_APPROVED, "Tresc", 1L);

        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationView view = useCase.execute(1L);

        assertThat(view.read()).isTrue();
    }

    @Test
    void execute_throwsForbidden_whenNotificationBelongsToSomeoneElse() {
        MarkNotificationAsReadUseCase useCase =
                new MarkNotificationAsReadUseCase(notificationRepository, currentUserProvider);
        Notification notification = Notification.create(7L, NotificationType.LEAVE_REQUEST_APPROVED, "Tresc", 1L);

        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(ForbiddenNotificationActionException.class);
    }

    @Test
    void execute_throwsNotFound_whenNotificationDoesNotExist() {
        MarkNotificationAsReadUseCase useCase =
                new MarkNotificationAsReadUseCase(notificationRepository, currentUserProvider);

        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(404L)).isInstanceOf(NotificationNotFoundException.class);
    }
}
