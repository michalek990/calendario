package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.timetracking.TimeEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TimeEntryRepositoryAdapter.class)
class TimeEntryRepositoryAdapterTest {

    @Autowired
    private TimeEntryRepositoryAdapter adapter;

    @Test
    void save_thenFindOpenEntryByUserId_returnsIt() {
        adapter.save(TimeEntry.clockIn(1L));

        TimeEntry open = adapter.findOpenEntryByUserId(1L).orElseThrow();

        assertThat(open.isOpen()).isTrue();
        assertThat(open.getUserId()).isEqualTo(1L);
    }

    @Test
    void findOpenEntryByUserId_returnsEmpty_afterClockOut() {
        TimeEntry saved = adapter.save(TimeEntry.clockIn(1L));
        adapter.save(saved.clockOut());

        assertThat(adapter.findOpenEntryByUserId(1L)).isEmpty();
    }

    @Test
    void findByUserId_returnsOnlyThatUsersEntries() {
        adapter.save(TimeEntry.clockIn(1L));
        adapter.save(TimeEntry.clockIn(2L));

        assertThat(adapter.findByUserId(1L)).hasSize(1);
        assertThat(adapter.findByUserId(1L).get(0).getUserId()).isEqualTo(1L);
    }
}
