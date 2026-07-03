package com.calendario.hrnest.domain.leave;

import java.util.List;
import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface LeaveRequestRepository {

    LeaveRequest save(LeaveRequest leaveRequest);

    Optional<LeaveRequest> findById(Long id);

    List<LeaveRequest> findByRequesterId(Long requesterId);

    List<LeaveRequest> findByStatus(LeaveStatus status);
}
