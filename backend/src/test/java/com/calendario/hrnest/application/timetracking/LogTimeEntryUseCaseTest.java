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
import com.calendario.hrnest.domain.timetracking.exception.InvalidTimeEntryRangeException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogTimeEntryUseCaseTest {

    private static final Instant START = Instant.parse("2026-08-03T08:00:00Z");
    private static final Instant END = Instant.parse("2026-08-03T16:00:00Z");

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private LogTimeEntryUseCase useCase() {
        return new LogTimeEntryUseCase(timeEntryRepository, projectRepository, currentUserProvider);
    }

    @Test
    void execute_createsClosedEntry_forGivenInstants() {
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new LogTimeEntryCommand(START, END, 30, null));

        assertThat(view.userId()).isEqualTo(1L);
        assertThat(view.clockOut()).isNotNull();
        assertThat(view.totalMinutes()).isEqualTo(8 * 60 - 30);
    }

    @Test
    void execute_defaultsBreakMinutesToZero_whenNotProvided() {
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new LogTimeEntryCommand(START, END, null, null));

        assertThat(view.totalMinutes()).isEqualTo(8 * 60);
    }

    @Test
    void execute_withProjectId_assignsProject_whenProjectExists() {
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(Project.create("Kalendario", null)));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntryView view = useCase().execute(new LogTimeEntryCommand(START, END, null, 5L));

        assertThat(view.projectId()).isEqualTo(5L);
    }

    @Test
    void execute_withUnknownProjectId_throwsProjectNotFound() {
        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new LogTimeEntryCommand(START, END, null, 404L)))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void execute_throwsInvalidRange_whenEndBeforeStart() {
        when(currentUserProvider.currentUserId()).thenReturn(1L);

        assertThatThrownBy(() -> useCase().execute(new LogTimeEntryCommand(END, START, null, null)))
                .isInstanceOf(InvalidTimeEntryRangeException.class);
    }
}
