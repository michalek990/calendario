package com.calendario.hrnest.application.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.InvalidCredentialsException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_savesNewHashedPassword_whenCurrentPasswordMatches() {
        ChangePasswordUseCase useCase = new ChangePasswordUseCase(userRepository, passwordHasher, currentUserProvider);
        User user = User.reconstitute(1L, "jan@example.com", "old-hash", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(currentUserProvider.currentUserEmail()).thenReturn("jan@example.com");
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("stareHaslo123", "old-hash")).thenReturn(true);
        when(passwordHasher.hash("noweHaslo123")).thenReturn("new-hash");

        useCase.execute(new ChangePasswordCommand("stareHaslo123", "noweHaslo123"));

        verify(userRepository).save(argThat(saved -> saved.getPasswordHash().equals("new-hash")));
    }

    @Test
    void execute_throwsInvalidCredentials_whenCurrentPasswordWrong() {
        ChangePasswordUseCase useCase = new ChangePasswordUseCase(userRepository, passwordHasher, currentUserProvider);
        User user = User.reconstitute(1L, "jan@example.com", "old-hash", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(currentUserProvider.currentUserEmail()).thenReturn("jan@example.com");
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("zlehaslo", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new ChangePasswordCommand("zlehaslo", "noweHaslo123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
