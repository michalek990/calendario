package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.leave.LeaveStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataLeaveRequestRepository extends JpaRepository<LeaveRequestJpaEntity, Long> {

    List<LeaveRequestJpaEntity> findByRequesterId(Long requesterId);

    List<LeaveRequestJpaEntity> findByStatus(LeaveStatus status);
}
