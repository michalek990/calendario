package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryManagementException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListManagedTimeEntriesUseCaseTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ListManagedTimeEntriesUseCase useCase() {
        return new ListManagedTimeEntriesUseCase(timeEntryRepository, userRepository, currentUserProvider);
    }

    @Test
    void execute_asHr_returnsEntriesOfOwnFacility_enrichedWithEmployeeData() {
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Hania", "Kadrowa", Role.HR, Instant.now());
        User employee = User.reconstitute(7L, "a@example.com", "hash", "Ala", "Nowak", Role.EMPLOYEE, Instant.now());
        TimeEntry entry = TimeEntry.reconstitute(1L, 7L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, null);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(userRepository.findAll()).thenReturn(List.of(hr, employee));
        when(timeEntryRepository.findAll()).thenReturn(List.of(entry));

        List<ManagedTimeEntryView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userFirstName()).isEqualTo("Ala");
        assertThat(result.get(0).userLastName()).isEqualTo("Nowak");
    }

    @Test
    void execute_asHr_excludesEntriesOfEmployeesInOtherFacilities() {
        User hr = User.reconstitute(5L, "hr@example.com", "hash", "Hania", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User sameFacility = User.reconstitute(7L, "a@example.com", "hash", "Ala", "Nowak", Role.EMPLOYEE,
                null, null, "Warszawa", null, Instant.now());
        User otherFacility = User.reconstitute(8L, "b@example.com", "hash", "Bob", "Adamski", Role.EMPLOYEE,
                null, null, "Krakow", null, Instant.now());
        TimeEntry sameFacilityEntry = TimeEntry.reconstitute(1L, 7L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, null);
        TimeEntry otherFacilityEntry = TimeEntry.reconstitute(2L, 8L,
                Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), 0, null, null);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(5L);
        when(userRepository.findAll()).thenReturn(List.of(hr, sameFacility, otherFacility));
        when(timeEntryRepository.findAll()).thenReturn(List.of(sameFacilityEntry, otherFacilityEntry));

        List<ManagedTimeEntryView> result = useCase().execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(7L);
    }

    @Test
    void execute_asManager_isAllowed() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(userRepository.findAll()).thenReturn(List.of());
        when(timeEntryRepository.findAll()).thenReturn(List.of());

        assertThat(useCase().execute()).isEmpty();
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute()).isInstanceOf(ForbiddenTimeEntryManagementException.class);
    }
}
