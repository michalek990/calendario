package com.calendario.hrnest.domain.notification;

import java.time.Instant;

/** Agregat domenowy — powiadomienie w aplikacji, niemutowalny (jak User i LeaveRequest). */
public final class Notification {

    private final Long id;
    private final Long recipientId;
    private final NotificationType type;
    private final String message;
    private final Long leaveRequestId;
    private final boolean read;
    private final Instant createdAt;

    private Notification(Long id, Long recipientId, NotificationType type, String message, Long leaveRequestId,
                          boolean read, Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = type;
        this.message = message;
        this.leaveRequestId = leaveRequestId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static Notification create(Long recipientId, NotificationType type, String message, Long leaveRequestId) {
        return new Notification(null, recipientId, type, message, leaveRequestId, false, Instant.now());
    }

    public static Notification reconstitute(Long id, Long recipientId, NotificationType type, String message,
                                             Long leaveRequestId, boolean read, Instant createdAt) {
        return new Notification(id, recipientId, type, message, leaveRequestId, read, createdAt);
    }

    public Notification markAsRead() {
        return new Notification(id, recipientId, type, message, leaveRequestId, true, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Long getLeaveRequestId() {
        return leaveRequestId;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
