package com.calendario.hrnest.domain.timetracking;

import java.util.List;
import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface TimeEntryRepository {

    TimeEntry save(TimeEntry timeEntry);

    Optional<TimeEntry> findOpenEntryByUserId(Long userId);

    List<TimeEntry> findByUserId(Long userId);

    List<TimeEntry> findByProjectId(Long projectId);
}
