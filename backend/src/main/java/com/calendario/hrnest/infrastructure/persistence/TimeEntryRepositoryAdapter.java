package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TimeEntryRepositoryAdapter implements TimeEntryRepository {

    private final SpringDataTimeEntryRepository springDataTimeEntryRepository;

    public TimeEntryRepositoryAdapter(SpringDataTimeEntryRepository springDataTimeEntryRepository) {
        this.springDataTimeEntryRepository = springDataTimeEntryRepository;
    }

    @Override
    public TimeEntry save(TimeEntry timeEntry) {
        return toDomain(springDataTimeEntryRepository.save(toEntity(timeEntry)));
    }

    @Override
    public Optional<TimeEntry> findOpenEntryByUserId(Long userId) {
        return springDataTimeEntryRepository.findFirstByUserIdAndClockOutIsNull(userId).map(this::toDomain);
    }

    @Override
    public List<TimeEntry> findByUserId(Long userId) {
        return springDataTimeEntryRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<TimeEntry> findByProjectId(Long projectId) {
        return springDataTimeEntryRepository.findByProjectId(projectId).stream()
                .map(this::toDomain)
                .toList();
    }

    private TimeEntryJpaEntity toEntity(TimeEntry timeEntry) {
        TimeEntryJpaEntity entity = new TimeEntryJpaEntity();
        entity.setId(timeEntry.getId());
        entity.setUserId(timeEntry.getUserId());
        entity.setClockIn(timeEntry.getClockIn());
        entity.setClockOut(timeEntry.getClockOut());
        entity.setBreakMinutes(timeEntry.getBreakMinutes());
        entity.setNotes(timeEntry.getNotes());
        entity.setProjectId(timeEntry.getProjectId());
        return entity;
    }

    private TimeEntry toDomain(TimeEntryJpaEntity entity) {
        return TimeEntry.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getClockIn(),
                entity.getClockOut(),
                entity.getBreakMinutes(),
                entity.getNotes(),
                entity.getProjectId());
    }
}
