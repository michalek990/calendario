package com.calendario.hrnest.application.timetracking;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryManagementException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Lista wpisów czasu pracy wszystkich pracowników — dla HR, MANAGER i ADMIN,
 * którzy wobec tej funkcji mają równe uprawnienia (bez ograniczenia MANAGER-a
 * do bezpośrednich podwładnych, w przeciwieństwie do modułu wniosków urlopowych).
 */
@Component
public class ListManagedTimeEntriesUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListManagedTimeEntriesUseCase(TimeEntryRepository timeEntryRepository, UserRepository userRepository,
                                          CurrentUserProvider currentUserProvider) {
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<ManagedTimeEntryView> execute() {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.MANAGER && role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenTimeEntryManagementException();
        }

        Map<Long, User> usersById = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return timeEntryRepository.findAll().stream()
                .map(entry -> ManagedTimeEntryView.from(entry, usersById.get(entry.getUserId())))
                .sorted(Comparator.comparing(ManagedTimeEntryView::clockIn).reversed())
                .toList();
    }
}
