package com.calendario.hrnest.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void register_hasNoOrganizationalDataByDefault() {
        User user = User.register("jan@example.com", "hash", "Jan", "Kowalski");

        assertThat(user.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(user.getPosition()).isNull();
        assertThat(user.getDepartment()).isNull();
        assertThat(user.getFacility()).isNull();
        assertThat(user.getSupervisorId()).isNull();
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
}
