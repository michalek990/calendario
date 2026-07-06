package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.leave.AnnualLeaveLimitPolicy;
import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GetAnnualLeaveSummaryUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public GetAnnualLeaveSummaryUseCase(LeaveRequestRepository leaveRequestRepository,
                                         CurrentUserProvider currentUserProvider) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public AnnualLeaveSummaryView execute(Integer requestedYear) {
        int year = requestedYear != null ? requestedYear : LocalDate.now().getYear();
        Long userId = currentUserProvider.currentUserId();

        List<LeaveRequest> approvedInYear = leaveRequestRepository.findByRequesterId(userId).stream()
                .filter(request -> request.getStatus() == LeaveStatus.APPROVED)
                .filter(request -> request.getStartDate().getYear() == year)
                .toList();

        Map<LeaveType, Long> daysByType = approvedInYear.stream()
                .collect(Collectors.groupingBy(LeaveRequest::getType, Collectors.summingLong(LeaveRequest::daysCount)));

        long remoteWorkDays = daysByType.getOrDefault(LeaveType.REMOTE_WORK, 0L);
        long otherLeaveDays = daysByType.entrySet().stream()
                .filter(entry -> entry.getKey() != LeaveType.REMOTE_WORK)
                .mapToLong(Map.Entry::getValue)
                .sum();

        long vacationDaysUsed = AnnualLeaveLimitPolicy.usedDays(approvedInYear, year);
        long vacationDaysRemaining = Math.max(0, AnnualLeaveLimitPolicy.ANNUAL_VACATION_LIMIT_DAYS - vacationDaysUsed);

        return new AnnualLeaveSummaryView(year, daysByType, remoteWorkDays, otherLeaveDays, vacationDaysUsed,
                vacationDaysRemaining, AnnualLeaveLimitPolicy.ANNUAL_VACATION_LIMIT_DAYS);
    }
}
