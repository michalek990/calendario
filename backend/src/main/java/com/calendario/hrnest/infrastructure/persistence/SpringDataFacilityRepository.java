package com.calendario.hrnest.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFacilityRepository extends JpaRepository<FacilityJpaEntity, Long> {

    boolean existsByName(String name);
}
