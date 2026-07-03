package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListMyTimeEntriesUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListMyTimeEntriesUseCase(TimeEntryRepository timeEntryRepository, CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<TimeEntryView> execute() {
        return timeEntryRepository.findByUserId(currentUserProvider.currentUserId()).stream()
                .map(TimeEntryView::from)
                .toList();
    }
}
