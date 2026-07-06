package com.calendario.hrnest.application.timetracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.project.Project;
import com.calendario.hrnest.domain.project.ProjectRepository;
import com.calendario.hrnest.domain.project.exception.ProjectNotFoundException;
import com.calendario.hrnest.domain.timetracking.TimeEntry;
import com.calendario.hrnest.domain.timetracking.TimeEntryRepository;
import com.calendario.hrnest.domain.timetracking.exception.ForbiddenTimeEntryActionException;
import com.calendario.hrnest.domain.timetracking.exception.InvalidTimeEntryRangeException;
import com.calendario.hrnest.domain.timetracking.exception.TimeEntryNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTimeEntryUseCaseTest {

    private static final Instant CLOCK_IN = Instant.parse("2026-08-03T08:00:00Z");
    private static final Instant CLOCK_OUT = Instant.parse("2026-08-03T16:00:00Z");

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UpdateTimeEntryUseCase useCase() {
        return new UpdateTimeEntryUseCase(timeEntryRepository, projectRepository, userRepository, currentUserProvider);
    }

    private TimeEntry existingEntry() {
        return TimeEntry.reconstitute(1L, 7L, CLOCK_IN, CLOCK_OUT, 0, null);
    }

    private User userWithFacility(Long id, String facility) {
        return User.reconstitute(id, "u" + id + "@example.com", "hash", "First", "Last", Role.EMPLOYEE,
                null, null, facility, null, Instant.now());
    }

    @Test
    void execute_updatesOwnEntry() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, 45, null));

        assertThat(view.breakMinutes()).isEqualTo(45);
        assertThat(view.totalMinutes()).isEqualTo(8 * 60 - 45);
    }

    @Test
    void execute_defaultsBreakMinutesToZero_whenNotProvided() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, null));

        assertThat(view.breakMinutes()).isEqualTo(0);
    }

    @Test
    void execute_withProjectId_assignsProject_whenProjectExists() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(Project.create("Kalendario", null)));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, 5L));

        assertThat(view.projectId()).isEqualTo(5L);
    }

    @Test
    void execute_withUnknownProjectId_throwsProjectNotFound() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);
        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, 404L)))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void execute_throwsInvalidRange_whenClockOutBeforeClockIn() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(7L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_OUT, CLOCK_IN, null, null)))
                .isInstanceOf(InvalidTimeEntryRangeException.class);
    }

    @Test
    void execute_throwsNotFound_whenEntryDoesNotExist() {
        when(timeEntryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateTimeEntryCommand(404L, CLOCK_IN, CLOCK_OUT, null, null)))
                .isInstanceOf(TimeEntryNotFoundException.class);
    }

    @Test
    void execute_throwsForbidden_whenEditingSomeoneElsesEntryAsEmployee() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, null)))
                .isInstanceOf(ForbiddenTimeEntryActionException.class);
    }

    @Test
    void execute_allowsManagerToUpdateSomeoneElsesEntry() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, 15, null));

        assertThat(view.breakMinutes()).isEqualTo(15);
    }

    @Test
    void execute_allowsHrToUpdateEntryOfSomeoneInOwnFacility() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(userRepository.findById(99L)).thenReturn(Optional.of(userWithFacility(99L, "Warszawa")));
        when(userRepository.findById(7L)).thenReturn(Optional.of(userWithFacility(7L, "Warszawa")));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, null))).isNotNull();
    }

    @Test
    void execute_throwsForbidden_whenHrUpdatesEntryOfSomeoneOutsideOwnFacility() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(userRepository.findById(99L)).thenReturn(Optional.of(userWithFacility(99L, "Warszawa")));
        when(userRepository.findById(7L)).thenReturn(Optional.of(userWithFacility(7L, "Krakow")));

        assertThatThrownBy(() -> useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, null)))
                .isInstanceOf(ForbiddenTimeEntryActionException.class);
    }

    @Test
    void execute_allowsAdminToUpdateSomeoneElsesEntry() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry()));
        when(currentUserProvider.currentUserId()).thenReturn(99L);
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(useCase().execute(new UpdateTimeEntryCommand(1L, CLOCK_IN, CLOCK_OUT, null, null))).isNotNull();
    }
}
