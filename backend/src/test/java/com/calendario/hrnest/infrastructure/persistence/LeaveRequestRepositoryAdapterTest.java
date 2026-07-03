package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.leave.LeaveRequest;
import com.calendario.hrnest.domain.leave.LeaveStatus;
import com.calendario.hrnest.domain.leave.LeaveType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(LeaveRequestRepositoryAdapter.class)
class LeaveRequestRepositoryAdapterTest {

    @Autowired
    private LeaveRequestRepositoryAdapter adapter;

    @Test
    void save_thenFindById_roundTripsAllFields() {
        LeaveRequest request = LeaveRequest.create(
                1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 7), "Wakacje");

        LeaveRequest saved = adapter.save(request);

        assertThat(saved.getId()).isNotNull();

        LeaveRequest found = adapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getRequesterId()).isEqualTo(1L);
        assertThat(found.getType()).isEqualTo(LeaveType.VACATION);
        assertThat(found.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(found.daysCount()).isEqualTo(5);
    }

    @Test
    void findByRequesterId_returnsOnlyThatRequestersRequests() {
        adapter.save(LeaveRequest.create(1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null));
        adapter.save(LeaveRequest.create(2L, LeaveType.SICK_LEAVE, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 4), null));

        assertThat(adapter.findByRequesterId(1L)).hasSize(1);
        assertThat(adapter.findByRequesterId(1L).get(0).getRequesterId()).isEqualTo(1L);
    }

    @Test
    void findByStatus_returnsOnlyMatchingStatus() {
        LeaveRequest pending = adapter.save(
                LeaveRequest.create(1L, LeaveType.VACATION, LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3), null));
        adapter.save(pending.approve(99L));
        adapter.save(LeaveRequest.create(2L, LeaveType.UNPAID, LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 5), null));

        assertThat(adapter.findByStatus(LeaveStatus.PENDING)).hasSize(1);
        assertThat(adapter.findByStatus(LeaveStatus.APPROVED)).hasSize(1);
    }
}
