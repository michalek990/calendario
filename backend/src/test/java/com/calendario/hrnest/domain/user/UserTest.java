package com.calendario.hrnest.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.user.exception.InvalidBirthDateException;
import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void register_hasNoOrganizationalOrPersonalDataByDefault() {
        User user = User.register("jan@example.com", "hash", "Jan", "Kowalski");

        assertThat(user.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(user.getPosition()).isNull();
        assertThat(user.getDepartment()).isNull();
        assertThat(user.getFacility()).isNull();
        assertThat(user.getSupervisorId()).isNull();
        assertThat(user.getBirthDate()).isNull();
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getLastLoginAt()).isNull();
    }

    @Test
    void updateOrganization_setsPositionDepartmentFacilityAndSupervisor() {
        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        User updated = user.updateOrganization("Programista", "IT", "Warszawa", 5L);

        assertThat(updated.getPosition()).isEqualTo("Programista");
        assertThat(updated.getDepartment()).isEqualTo("IT");
        assertThat(updated.getFacility()).isEqualTo("Warszawa");
        assertThat(updated.getSupervisorId()).isEqualTo(5L);
    }

    @Test
    void updateOrganization_throwsInvalidSupervisorAssignment_whenSelfSupervision() {
        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.MANAGER,
                Instant.now());

        assertThatThrownBy(() -> user.updateOrganization("Kierownik", "IT", "Warszawa", 1L))
                .isInstanceOf(InvalidSupervisorAssignmentException.class);
    }

    @Test
    void updatePersonalInfo_setsBirthDatePhoneAndAvatar() {
        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        User updated = user.updatePersonalInfo(LocalDate.of(1990, 5, 1), "+48123456789", "https://example.com/a.png");

        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(updated.getPhoneNumber()).isEqualTo("+48123456789");
        assertThat(updated.getAvatarUrl()).isEqualTo("https://example.com/a.png");
    }

    @Test
    void updatePersonalInfo_throwsInvalidBirthDate_whenInTheFuture() {
        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        assertThatThrownBy(() -> user.updatePersonalInfo(LocalDate.now().plusDays(1), null, null))
                .isInstanceOf(InvalidBirthDateException.class);
    }

    @Test
    void recordLogin_setsLastLoginAt() {
        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());
        Instant loginAt = Instant.parse("2026-07-06T10:00:00Z");

        User updated = user.recordLogin(loginAt);

        assertThat(updated.getLastLoginAt()).isEqualTo(loginAt);
    }
}
