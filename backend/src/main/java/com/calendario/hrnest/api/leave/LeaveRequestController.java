package com.calendario.hrnest.api.leave;

import com.calendario.hrnest.application.leave.ApproveLeaveRequestUseCase;
import com.calendario.hrnest.application.leave.CreateLeaveRequestCommand;
import com.calendario.hrnest.application.leave.CreateLeaveRequestUseCase;
import com.calendario.hrnest.application.leave.LeaveRequestView;
import com.calendario.hrnest.application.leave.ListMyLeaveRequestsUseCase;
import com.calendario.hrnest.application.leave.ListPendingLeaveRequestsUseCase;
import com.calendario.hrnest.application.leave.ListRecentLeaveActivityUseCase;
import com.calendario.hrnest.application.leave.RejectLeaveRequestUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final CreateLeaveRequestUseCase createLeaveRequestUseCase;
    private final ApproveLeaveRequestUseCase approveLeaveRequestUseCase;
    private final RejectLeaveRequestUseCase rejectLeaveRequestUseCase;
    private final ListMyLeaveRequestsUseCase listMyLeaveRequestsUseCase;
    private final ListPendingLeaveRequestsUseCase listPendingLeaveRequestsUseCase;
    private final ListRecentLeaveActivityUseCase listRecentLeaveActivityUseCase;

    public LeaveRequestController(
            CreateLeaveRequestUseCase createLeaveRequestUseCase,
            ApproveLeaveRequestUseCase approveLeaveRequestUseCase,
            RejectLeaveRequestUseCase rejectLeaveRequestUseCase,
            ListMyLeaveRequestsUseCase listMyLeaveRequestsUseCase,
            ListPendingLeaveRequestsUseCase listPendingLeaveRequestsUseCase,
            ListRecentLeaveActivityUseCase listRecentLeaveActivityUseCase) {
        this.createLeaveRequestUseCase = createLeaveRequestUseCase;
        this.approveLeaveRequestUseCase = approveLeaveRequestUseCase;
        this.rejectLeaveRequestUseCase = rejectLeaveRequestUseCase;
        this.listMyLeaveRequestsUseCase = listMyLeaveRequestsUseCase;
        this.listPendingLeaveRequestsUseCase = listPendingLeaveRequestsUseCase;
        this.listRecentLeaveActivityUseCase = listRecentLeaveActivityUseCase;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestView> create(@Valid @RequestBody CreateLeaveRequestRequest request) {
        LeaveRequestView created = createLeaveRequestUseCase.execute(
                new CreateLeaveRequestCommand(request.type(), request.startDate(), request.endDate(), request.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<List<LeaveRequestView>> listMine() {
        return ResponseEntity.ok(listMyLeaveRequestsUseCase.execute());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestView>> listPending() {
        return ResponseEntity.ok(listPendingLeaveRequestsUseCase.execute());
    }

    @GetMapping("/me/recent-activity")
    public ResponseEntity<List<LeaveRequestView>> recentActivity() {
        return ResponseEntity.ok(listRecentLeaveActivityUseCase.execute());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestView> approve(@PathVariable Long id) {
        return ResponseEntity.ok(approveLeaveRequestUseCase.execute(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestView> reject(@PathVariable Long id) {
        return ResponseEntity.ok(rejectLeaveRequestUseCase.execute(id));
    }
}
