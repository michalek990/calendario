package com.calendario.hrnest.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    List<NotificationJpaEntity> findByRecipientId(Long recipientId);
}
