package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryAlreadyOpenException;
import org.springframework.stereotype.Component;

@Component
public class ClockInUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CurrentUserProvider currentUserProvider;

    public ClockInUseCase(TimeEntryRepository timeEntryRepository, CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public TimeEntryView execute() {
        Long userId = currentUserProvider.currentUserId();

        if (timeEntryRepository.findOpenEntryByUserId(userId).isPresent()) {
            throw new TimeEntryAlreadyOpenException();
        }

        TimeEntry saved = timeEntryRepository.save(TimeEntry.clockIn(userId));

        return TimeEntryView.from(saved);
    }
}
