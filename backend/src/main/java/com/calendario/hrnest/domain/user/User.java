package com.calendario.hrnest.domain.user;

import java.time.Instant;

/**
 * Agregat domenowy — bez adnotacji JPA/Springa. Tworzony wyłącznie przez
 * metody fabryczne, żeby nie dało się zbudować obiektu w niespójnym stanie.
 */
public final class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final Role role;
    private final Instant createdAt;

    private User(Long id, String email, String passwordHash, String firstName, String lastName,
                  Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = createdAt;
    }

    /** Rejestracja nowego użytkownika — zawsze z rolą EMPLOYEE, id nadawane przez repozytorium. */
    public static User register(String email, String passwordHash, String firstName, String lastName) {
        return new User(null, email, passwordHash, firstName, lastName, Role.EMPLOYEE, Instant.now());
    }

    /** Odtworzenie encji z warstwy persystencji — omija reguły rejestracyjne. */
    public static User reconstitute(Long id, String email, String passwordHash, String firstName,
                                     String lastName, Role role, Instant createdAt) {
        return new User(id, email, passwordHash, firstName, lastName, role, createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
