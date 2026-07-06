package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Zwraca ostatnie zmiany na wnioskach urlopowych (w tym praca z domu, skoro
 * {@code REMOTE_WORK} jest jednym z {@link com.calendario.hrnest.domain.leave.LeaveType})
 * aktualnie zalogowanego użytkownika — posortowane po dacie ostatniej zmiany malejąco.
 */
@Component
public class ListRecentLeaveActivityUseCase {

    private static final int DEFAULT_LIMIT = 10;

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ListRecentLeaveActivityUseCase(LeaveRequestRepository leaveRequestRepository,
                                           CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<LeaveRequestView> execute() {
        return leaveRequestRepository.findByRequesterId(currentUserProvider.currentUserId()).stream()
                .map(LeaveRequestView::from)
                .sorted(Comparator.comparing(ListRecentLeaveActivityUseCase::lastActivityAt).reversed())
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private static Instant lastActivityAt(LeaveRequestView view) {
        return view.approvedAt() != null && view.approvedAt().isAfter(view.createdAt())
                ? view.approvedAt()
                : view.createdAt();
    }
}
