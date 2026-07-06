package com.calendario.hrnest.domain.facility;

import java.util.List;
import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface FacilityRepository {

    Facility save(Facility facility);

    Optional<Facility> findById(Long id);

    List<Facility> findAll();

    boolean existsByName(String name);

    void deleteById(Long id);
}
