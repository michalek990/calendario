package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveRequestRepository;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LeaveRequestRepositoryAdapter implements LeaveRequestRepository {

    private final SpringDataLeaveRequestRepository springDataLeaveRequestRepository;

    public LeaveRequestRepositoryAdapter(SpringDataLeaveRequestRepository springDataLeaveRequestRepository) {
        this.springDataLeaveRequestRepository = springDataLeaveRequestRepository;
    }

    @Override
    public LeaveRequest save(LeaveRequest leaveRequest) {
        return toDomain(springDataLeaveRequestRepository.save(toEntity(leaveRequest)));
    }

    @Override
    public Optional<LeaveRequest> findById(Long id) {
        return springDataLeaveRequestRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<LeaveRequest> findByRequesterId(Long requesterId) {
        return springDataLeaveRequestRepository.findByRequesterId(requesterId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<LeaveRequest> findByStatus(LeaveStatus status) {
        return springDataLeaveRequestRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .toList();
    }

    private LeaveRequestJpaEntity toEntity(LeaveRequest leaveRequest) {
        LeaveRequestJpaEntity entity = new LeaveRequestJpaEntity();
        entity.setId(leaveRequest.getId());
        entity.setRequesterId(leaveRequest.getRequesterId());
        entity.setType(leaveRequest.getType());
        entity.setStartDate(leaveRequest.getStartDate());
        entity.setEndDate(leaveRequest.getEndDate());
        entity.setStatus(leaveRequest.getStatus());
        entity.setReason(leaveRequest.getReason());
        entity.setApproverId(leaveRequest.getApproverId());
        entity.setApprovedAt(leaveRequest.getApprovedAt());
        entity.setCreatedAt(leaveRequest.getCreatedAt());
        return entity;
    }

    private LeaveRequest toDomain(LeaveRequestJpaEntity entity) {
        return LeaveRequest.reconstitute(
                entity.getId(),
                entity.getRequesterId(),
                entity.getType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getReason(),
                entity.getApproverId(),
                entity.getApprovedAt(),
                entity.getCreatedAt());
    }
}
