package com.calendario.hrnest.domain.user;

import com.calendario.hrnest.domain.user.exception.InvalidBirthDateException;
import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
import java.time.Instant;
import java.time.LocalDate;

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
    private final LocalDate birthDate;
    private final String phoneNumber;
    private final String avatarUrl;
    private final Instant lastLoginAt;
    private final Instant createdAt;

    private User(Long id, String email, String passwordHash, String firstName, String lastName,
                  Role role, String position, String department, String facility, Long supervisorId,
                  LocalDate birthDate, String phoneNumber, String avatarUrl, Instant lastLoginAt,
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
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    /**
     * Rejestracja nowego użytkownika — zawsze z rolą EMPLOYEE, id nadawane przez
     * repozytorium. Dane organizacyjne i personalne nie są znane w momencie
     * rejestracji — uzupełniane później przez {@link #updateOrganization} (HR/ADMIN)
     * i {@link #updatePersonalInfo} (sam użytkownik).
     */
    public static User register(String email, String passwordHash, String firstName, String lastName) {
        return new User(null, email, passwordHash, firstName, lastName, Role.EMPLOYEE, null, null, null, null,
                null, null, null, null, Instant.now());
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
        return reconstitute(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, null, null, null, null, createdAt);
    }

    /** Odtworzenie encji z warstwy persystencji — wraz z danymi organizacyjnymi i personalnymi. */
    public static User reconstitute(Long id, String email, String passwordHash, String firstName, String lastName,
                                     Role role, String position, String department, String facility,
                                     Long supervisorId, LocalDate birthDate, String phoneNumber, String avatarUrl,
                                     Instant lastLoginAt, Instant createdAt) {
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, lastLoginAt, createdAt);
    }

    public User changePassword(String newPasswordHash) {
        return new User(id, email, newPasswordHash, firstName, lastName, role, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, lastLoginAt, createdAt);
    }

    /**
     * Aktualizuje dane organizacyjne (stanowisko, dział, zakład, przełożony).
     * Wykonywane wyłącznie przez HR/ADMIN — sprawdzane w use case'ie, nie tutaj.
     */
    public User updateOrganization(String position, String department, String facility, Long supervisorId) {
        if (supervisorId != null && supervisorId.equals(id)) {
            throw new InvalidSupervisorAssignmentException();
        }
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, lastLoginAt, createdAt);
    }

    /** Aktualizuje dane personalne (data urodzenia, telefon, awatar) — edytowalne samodzielnie przez użytkownika. */
    public User updatePersonalInfo(LocalDate birthDate, String phoneNumber, String avatarUrl) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new InvalidBirthDateException();
        }
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, lastLoginAt, createdAt);
    }

    /** Zmienia rolę użytkownika (EMPLOYEE/MANAGER/HR/ADMIN) — wyłącznie ADMIN, sprawdzane w use case'ie. */
    public User updateRole(Role newRole) {
        return new User(id, email, passwordHash, firstName, lastName, newRole, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, lastLoginAt, createdAt);
    }

    /** Zwraca kopię z zaktualizowanym znacznikiem ostatniego logowania. */
    public User recordLogin(Instant loginAt) {
        return new User(id, email, passwordHash, firstName, lastName, role, position, department, facility,
                supervisorId, birthDate, phoneNumber, avatarUrl, loginAt, createdAt);
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
