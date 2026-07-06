package com.calendario.hrnest.api.timetracking;

import com.calendario.hrnest.application.project.ProjectTimeSummaryView;
import com.calendario.hrnest.application.timetracking.ClockInUseCase;
import com.calendario.hrnest.application.timetracking.ClockOutUseCase;
import com.calendario.hrnest.application.timetracking.ListManagedTimeEntriesUseCase;
import com.calendario.hrnest.application.timetracking.ListMyTimeByProjectUseCase;
import com.calendario.hrnest.application.timetracking.ListMyTimeEntriesUseCase;
import com.calendario.hrnest.application.timetracking.LogTimeEntryCommand;
import com.calendario.hrnest.application.timetracking.LogTimeEntryUseCase;
import com.calendario.hrnest.application.timetracking.ManagedTimeEntryView;
import com.calendario.hrnest.application.timetracking.TimeEntryView;
import com.calendario.hrnest.application.timetracking.UpdateTimeEntryCommand;
import com.calendario.hrnest.application.timetracking.UpdateTimeEntryUseCase;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final ClockInUseCase clockInUseCase;
    private final ClockOutUseCase clockOutUseCase;
    private final ListMyTimeEntriesUseCase listMyTimeEntriesUseCase;
    private final ListMyTimeByProjectUseCase listMyTimeByProjectUseCase;
    private final LogTimeEntryUseCase logTimeEntryUseCase;
    private final UpdateTimeEntryUseCase updateTimeEntryUseCase;
    private final ListManagedTimeEntriesUseCase listManagedTimeEntriesUseCase;

    public TimeEntryController(ClockInUseCase clockInUseCase, ClockOutUseCase clockOutUseCase,
                                ListMyTimeEntriesUseCase listMyTimeEntriesUseCase,
                                ListMyTimeByProjectUseCase listMyTimeByProjectUseCase,
                                LogTimeEntryUseCase logTimeEntryUseCase,
                                UpdateTimeEntryUseCase updateTimeEntryUseCase,
                                ListManagedTimeEntriesUseCase listManagedTimeEntriesUseCase) {
        this.clockInUseCase = clockInUseCase;
        this.clockOutUseCase = clockOutUseCase;
        this.listMyTimeEntriesUseCase = listMyTimeEntriesUseCase;
        this.listMyTimeByProjectUseCase = listMyTimeByProjectUseCase;
        this.logTimeEntryUseCase = logTimeEntryUseCase;
        this.updateTimeEntryUseCase = updateTimeEntryUseCase;
        this.listManagedTimeEntriesUseCase = listManagedTimeEntriesUseCase;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<TimeEntryView> clockIn(@RequestBody(required = false) ClockInRequest request) {
        Long projectId = request != null ? request.projectId() : null;
        Instant clockInAt = request != null ? request.clockIn() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(clockInUseCase.execute(projectId, clockInAt));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<TimeEntryView> clockOut(@RequestBody(required = false) ClockOutRequest request) {
        Instant clockOutAt = request != null ? request.clockOut() : null;
        return ResponseEntity.ok(clockOutUseCase.execute(clockOutAt));
    }

    @PostMapping("/log")
    public ResponseEntity<TimeEntryView> log(@Valid @RequestBody LogTimeEntryRequest request) {
        TimeEntryView created = logTimeEntryUseCase.execute(new LogTimeEntryCommand(
                request.clockIn(), request.clockOut(), request.breakMinutes(), request.projectId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntryView> update(@PathVariable Long id, @Valid @RequestBody UpdateTimeEntryRequest request) {
        TimeEntryView updated = updateTimeEntryUseCase.execute(new UpdateTimeEntryCommand(
                id, request.clockIn(), request.clockOut(), request.breakMinutes(), request.projectId()));
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimeEntryView>> listMine() {
        return ResponseEntity.ok(listMyTimeEntriesUseCase.execute());
    }

    @GetMapping("/me/by-project")
    public ResponseEntity<List<ProjectTimeSummaryView>> listMineByProject() {
        return ResponseEntity.ok(listMyTimeByProjectUseCase.execute());
    }

    /** Wpisy czasu pracy wszystkich pracowników — dla HR/MANAGER/ADMIN, do zakładki zarządzania zespołem. */
    @GetMapping
    public ResponseEntity<List<ManagedTimeEntryView>> listManaged() {
        return ResponseEntity.ok(listManagedTimeEntriesUseCase.execute());
    }
}
