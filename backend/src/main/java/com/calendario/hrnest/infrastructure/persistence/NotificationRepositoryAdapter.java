package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.notification.Notification;
import com.calendario.hrnest.domain.notification.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository springDataNotificationRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationRepository springDataNotificationRepository) {
        this.springDataNotificationRepository = springDataNotificationRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return toDomain(springDataNotificationRepository.save(toEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return springDataNotificationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByRecipientId(Long recipientId) {
        return springDataNotificationRepository.findByRecipientId(recipientId).stream()
                .map(this::toDomain)
                .toList();
    }

    private NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setRecipientId(notification.getRecipientId());
        entity.setType(notification.getType());
        entity.setMessage(notification.getMessage());
        entity.setLeaveRequestId(notification.getLeaveRequestId());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        return entity;
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
                entity.getId(),
                entity.getRecipientId(),
                entity.getType(),
                entity.getMessage(),
                entity.getLeaveRequestId(),
                entity.isRead(),
                entity.getCreatedAt());
    }
}
