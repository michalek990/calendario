package com.calendario.hrnest.api.timetracking;

import com.calendario.hrnest.application.project.ProjectTimeSummaryView;
import com.calendario.hrnest.application.timetracking.ClockInUseCase;
import com.calendario.hrnest.application.timetracking.ClockOutUseCase;
import com.calendario.hrnest.application.timetracking.ListMyTimeByProjectUseCase;
import com.calendario.hrnest.application.timetracking.ListMyTimeEntriesUseCase;
import com.calendario.hrnest.application.timetracking.TimeEntryView;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    public TimeEntryController(ClockInUseCase clockInUseCase, ClockOutUseCase clockOutUseCase,
                                ListMyTimeEntriesUseCase listMyTimeEntriesUseCase,
                                ListMyTimeByProjectUseCase listMyTimeByProjectUseCase) {
        this.clockInUseCase = clockInUseCase;
        this.clockOutUseCase = clockOutUseCase;
        this.listMyTimeEntriesUseCase = listMyTimeEntriesUseCase;
        this.listMyTimeByProjectUseCase = listMyTimeByProjectUseCase;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<TimeEntryView> clockIn(@RequestBody(required = false) ClockInRequest request) {
        Long projectId = request != null ? request.projectId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(clockInUseCase.execute(projectId));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<TimeEntryView> clockOut() {
        return ResponseEntity.ok(clockOutUseCase.execute());
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimeEntryView>> listMine() {
        return ResponseEntity.ok(listMyTimeEntriesUseCase.execute());
    }

    @GetMapping("/me/by-project")
    public ResponseEntity<List<ProjectTimeSummaryView>> listMineByProject() {
        return ResponseEntity.ok(listMyTimeByProjectUseCase.execute());
    }
}
