package com.calendario.hrnest.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveType;
import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import com.calendario.hrnest.domain.notification.NotificationType;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveDecisionNotifierTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailSender emailSender;

    @Test
    void notifyDecision_createsApprovedNotification_andSendsEmail() {
        LeaveDecisionNotifier notifier = new LeaveDecisionNotifier(userRepository, notificationRepository, emailSender);

        LeaveRequest approved = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7), null)
                .approve(2L);

        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());
        User approver = User.reconstitute(2L, "szef@example.com", "hash", "Ala", "Szefowa", Role.MANAGER,
                Instant.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notifier.notifyDecision(approved);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getRecipientId()).isEqualTo(1L);
        assertThat(saved.getType()).isEqualTo(NotificationType.LEAVE_REQUEST_APPROVED);
        assertThat(saved.getMessage()).contains("Ala Szefowa").contains("zaakceptowany");

        verify(emailSender).send(eq("pracownik@example.com"), anyString(), anyString());
    }

    @Test
    void notifyDecision_createsRejectedNotification() {
        LeaveDecisionNotifier notifier = new LeaveDecisionNotifier(userRepository, notificationRepository, emailSender);

        LeaveRequest rejected = LeaveRequest.create(
                1L, LeaveType.REMOTE_WORK, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null)
                .reject(2L);

        User requester = User.reconstitute(1L, "pracownik@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notifier.notifyDecision(rejected);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getType()).isEqualTo(NotificationType.LEAVE_REQUEST_REJECTED);
        assertThat(notificationCaptor.getValue().getMessage()).contains("odrzucony");
    }

    @Test
    void notifyDecision_doesNotSendEmail_whenRequesterNoLongerExists() {
        LeaveDecisionNotifier notifier = new LeaveDecisionNotifier(userRepository, notificationRepository, emailSender);

        LeaveRequest approved = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7), null)
                .approve(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notifier.notifyDecision(approved);

        verify(emailSender, never()).send(anyString(), anyString(), anyString());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
