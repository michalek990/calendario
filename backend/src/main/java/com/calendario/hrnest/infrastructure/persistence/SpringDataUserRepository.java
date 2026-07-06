package com.calendario.hrnest.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsBySupervisorId(Long supervisorId);

    boolean existsByFacility(String facility);

    List<UserJpaEntity> findByFacility(String facility);
}
