package com.calendario.hrnest.domain.project;

import java.util.List;
import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(Long id);

    List<Project> findAll();

    boolean existsByName(String name);
}
