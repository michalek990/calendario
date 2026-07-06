package com.calendario.hrnest.application.notification;

import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import com.calendario.hrnest.domain.notification.NotificationType;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Powiadamia wnioskodawcę o decyzji na jego wniosku — zarówno w aplikacji
 * (Notification), jak i mailowo (EmailSender). Wywoływane przez
 * ApproveLeaveRequestUseCase / RejectLeaveRequestUseCase po zapisaniu decyzji.
 */
@Component
public class LeaveDecisionNotifier {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public LeaveDecisionNotifier(UserRepository userRepository, NotificationRepository notificationRepository,
                                  EmailSender emailSender) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    public void notifyDecision(LeaveRequest leaveRequest) {
        boolean approved = leaveRequest.getStatus() == LeaveStatus.APPROVED;
        NotificationType type = approved ? NotificationType.LEAVE_REQUEST_APPROVED : NotificationType.LEAVE_REQUEST_REJECTED;

        String approverName = approverFullName(leaveRequest.getApproverId());
        String message = "Twój wniosek (%s, %s – %s) został %s przez %s.".formatted(
                leaveRequest.getType().polishLabel(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                approved ? "zaakceptowany" : "odrzucony",
                approverName);

        notificationRepository.save(Notification.create(leaveRequest.getRequesterId(), type, message,
                leaveRequest.getId()));

        userRepository.findById(leaveRequest.getRequesterId()).ifPresent(requester -> {
            String subject = approved ? "Twój wniosek został zaakceptowany" : "Twój wniosek został odrzucony";
            emailSender.send(requester.getEmail(), subject, message);
        });
    }

    private String approverFullName(Long approverId) {
        if (approverId == null) {
            return "przełożonego";
        }
        return userRepository.findById(approverId)
                .map(approver -> approver.getFirstName() + " " + approver.getLastName())
                .orElse("przełożonego");
    }
}
