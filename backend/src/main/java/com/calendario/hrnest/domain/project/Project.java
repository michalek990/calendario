package com.calendario.hrnest.domain.project;

import java.time.Instant;

/** Agregat domenowy — bez adnotacji JPA/Springa, niemutowalny (jak User/LeaveRequest/TimeEntry). */
public final class Project {

    private final Long id;
    private final String name;
    private final String description;
    private final Instant createdAt;

    private Project(Long id, String name, String description, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static Project create(String name, String description) {
        return new Project(null, name, description, Instant.now());
    }

    public static Project reconstitute(Long id, String name, String description, Instant createdAt) {
        return new Project(id, name, description, createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
