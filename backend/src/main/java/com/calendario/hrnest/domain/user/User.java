package com.calendario.hrnest.domain.user;

import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
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
    private final String position;
    private final String department;
    private final String facility;
    private final Long supervisorId;
    private final Instant createdAt;

    private User(Long id, String email, String passwordHash, String firstName, String lastName,
                  Role role, String position, String department, String facility, Long supervisorId,
                  Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.position = position;
        this.department = department;
        this.facility = facility;
        this.supervisorId = supervisorId;
        this.createdAt = createdAt;
    }

    /**
     * Rejestracja nowego użytkownika — zawsze z rolą EMPLOYEE, id nadawane przez
     * repozytorium. Dane organizacyjne (stanowisko, dział, zakład, przełożony)
     * nie są znane w momencie rejestracji — uzupełnia je HR przez
     * {@link #updateOrganization(String, String, String, Long)}.
     */
    public static User register(String email, String passwordHash, String firstName, String lastName) {
        return new User(null, email, passwordHash, firstName, lastName, Role.EMPLOYEE, null, null, null, null,
                Instant.now());
    }

    /** Odtworzenie encji z warstwy persystencji — omija reguły rejestracyjne. */
    public static User reconstitute(Long id, String email, String passwordHash, String firstName,
                                     String lastName, Role role, Instant createdAt) {
        return reconstitute(id, email, passwordHash, firstName, lastName, role, null, null, null, null, createdAt);
    }

    /** Odtworzenie encji z warstwy persystencji — wraz z danymi organizacyjnymi. */
    public static User reconstitute(Long id, String email, String passwordHash, String firstName, String lastName,
                                     Role role, String position, String department, String facility,
                                     Long supervisorId, Instant createdAt) {
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, createdAt);
    }

    public User changePassword(String newPasswordHash) {
        return new User(id, email, newPasswordHash, firstName, lastName, role, position, department, facility,
                supervisorId, createdAt);
    }

    /**
     * Aktualizuje dane organizacyjne (stanowisko, dział, zakład, przełożony).
     * Wykonywane wyłącznie przez HR_ADMIN — sprawdzane w use case'ie, nie tutaj.
     */
    public User updateOrganization(String position, String department, String facility, Long supervisorId) {
        if (supervisorId != null && supervisorId.equals(id)) {
            throw new InvalidSupervisorAssignmentException();
        }
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, createdAt);
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

    public String getPosition() {
        return position;
    }

    public String getDepartment() {
        return department;
    }

    public String getFacility() {
        return facility;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
