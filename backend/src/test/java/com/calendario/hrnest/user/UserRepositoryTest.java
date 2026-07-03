package com.calendario.hrnest.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        User user = new User("jan.kowalski@example.com", "hashed-password", "Jan", "Kowalski", Role.EMPLOYEE);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("jan.kowalski@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jan");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("nieistnieje@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenUserWithEmailExists() {
        userRepository.save(new User("anna.nowak@example.com", "hashed-password", "Anna", "Nowak", Role.MANAGER));

        assertThat(userRepository.existsByEmail("anna.nowak@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("ktos.inny@example.com")).isFalse();
    }

    @Test
    void save_rejectsDuplicateEmail() {
        userRepository.saveAndFlush(new User("duplikat@example.com", "hash1", "A", "B", Role.EMPLOYEE));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("duplikat@example.com", "hash2", "C", "D", Role.EMPLOYEE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
