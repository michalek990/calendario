package com.calendario.hrnest.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Encja JPA — mapowana z/do domain.timetracking.TimeEntry przez TimeEntryRepositoryAdapter. */
@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
public class TimeEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "clock_in", nullable = false)
    private Instant clockIn;

    @Column(name = "clock_out")
    private Instant clockOut;

    @Column(name = "break_minutes", nullable = false)
    private int breakMinutes;

    private String notes;

    @Column(name = "project_id")
    private Long projectId;
}
