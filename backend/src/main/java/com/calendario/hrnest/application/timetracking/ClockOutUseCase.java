package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.NoOpenTimeEntryException;
import org.springframework.stereotype.Component;

@Component
public class ClockOutUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CurrentUserProvider currentUserProvider;

    public ClockOutUseCase(TimeEntryRepository timeEntryRepository, CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public TimeEntryView execute() {
        TimeEntry openEntry = timeEntryRepository.findOpenEntryByUserId(currentUserProvider.currentUserId())
                .orElseThrow(NoOpenTimeEntryException::new);

        TimeEntry closed = timeEntryRepository.save(openEntry.clockOut());

        return TimeEntryView.from(closed);
    }
}
