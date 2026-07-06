package com.calendario.hrnest.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProjectRepository extends JpaRepository<ProjectJpaEntity, Long> {

    boolean existsByName(String name);
}
