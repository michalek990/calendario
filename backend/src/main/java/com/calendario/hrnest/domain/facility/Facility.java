package com.calendario.hrnest.domain.facility;

import java.time.Instant;

/** Agregat domenowy — bez adnotacji JPA/Springa, niemutowalny (jak Project). */
public final class Facility {

    private final Long id;
    private final String name;
    private final Instant createdAt;

    private Facility(Long id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Facility create(String name) {
        return new Facility(null, name, Instant.now());
    }

    public static Facility reconstitute(Long id, String name, Instant createdAt) {
        return new Facility(id, name, createdAt);
    }

    public Facility rename(String newName) {
        return new Facility(id, newName, createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
