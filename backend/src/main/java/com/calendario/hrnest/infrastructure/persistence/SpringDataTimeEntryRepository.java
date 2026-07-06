package com.calendario.hrnest.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTimeEntryRepository extends JpaRepository<TimeEntryJpaEntity, Long> {

    Optional<TimeEntryJpaEntity> findFirstByUserIdAndClockOutIsNull(Long userId);

    List<TimeEntryJpaEntity> findByUserId(Long userId);

    List<TimeEntryJpaEntity> findByProjectId(Long projectId);
}
