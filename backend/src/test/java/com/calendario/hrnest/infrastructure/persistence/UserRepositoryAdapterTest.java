package com.calendario.hrnest.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
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
}
