package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void save_thenFindByEmail_roundTripsAllFields() {
        User user = User.register("jan.kowalski@example.com", "hashed-password", "Jan", "Kowalski");

        User saved = userRepositoryAdapter.save(user);

        assertThat(saved.getId()).isNotNull();

        User found = userRepositoryAdapter.findByEmail("jan.kowalski@example.com").orElseThrow();
        assertThat(found.getFirstName()).isEqualTo("Jan");
        assertThat(found.getLastName()).isEqualTo("Kowalski");
        assertThat(found.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        assertThat(userRepositoryAdapter.findByEmail("nieistnieje@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_reflectsSavedUsers() {
        userRepositoryAdapter.save(User.register("anna.nowak@example.com", "hashed", "Anna", "Nowak"));

        assertThat(userRepositoryAdapter.existsByEmail("anna.nowak@example.com")).isTrue();
        assertThat(userRepositoryAdapter.existsByEmail("ktos.inny@example.com")).isFalse();
    }

    @Test
    void save_thenFindById_roundTripsOrganizationalFields() {
        User supervisor = userRepositoryAdapter.save(
                User.register("szef@example.com", "hashed", "Ala", "Szefowa"));

        User user = User.register("piotr@example.com", "hashed", "Piotr", "Nowicki")
                .updateOrganization("Programista", "IT", "Warszawa", supervisor.getId());
        User saved = userRepositoryAdapter.save(user);

        User found = userRepositoryAdapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getPosition()).isEqualTo("Programista");
        assertThat(found.getDepartment()).isEqualTo("IT");
        assertThat(found.getFacility()).isEqualTo("Warszawa");
        assertThat(found.getSupervisorId()).isEqualTo(supervisor.getId());
    }

    @Test
    void findById_returnsEmpty_whenIdDoesNotExist() {
        assertThat(userRepositoryAdapter.findById(-1L)).isEmpty();
    }

    @Test
    void existsBySupervisorId_trueOnlyWhenSomeoneReportsToThatUser() {
        User supervisor = userRepositoryAdapter.save(
                User.register("kierownik@example.com", "hashed", "Ala", "Kierownik"));
        User subordinate = User.register("podwladny@example.com", "hashed", "Jan", "Podwladny")
                .updateOrganization(null, null, null, supervisor.getId());
        User savedSubordinate = userRepositoryAdapter.save(subordinate);

        assertThat(userRepositoryAdapter.existsBySupervisorId(supervisor.getId())).isTrue();
        assertThat(userRepositoryAdapter.existsBySupervisorId(savedSubordinate.getId())).isFalse();
    }

    @Test
    void save_thenFindById_roundTripsPersonalFields() {
        Instant loginAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        User user = User.register("kontakt@example.com", "hashed", "Ewa", "Testowa")
                .updatePersonalInfo(LocalDate.of(1990, 5, 1), "+48123456789", "https://example.com/a.png")
                .recordLogin(loginAt);

        User saved = userRepositoryAdapter.save(user);

        User found = userRepositoryAdapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(found.getPhoneNumber()).isEqualTo("+48123456789");
        assertThat(found.getAvatarUrl()).isEqualTo("https://example.com/a.png");
        assertThat(found.getLastLoginAt()).isEqualTo(loginAt);
    }
}
