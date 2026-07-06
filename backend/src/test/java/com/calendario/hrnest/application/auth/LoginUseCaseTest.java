package com.calendario.hrnest.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.InvalidCredentialsException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    void execute_returnsToken_forValidCredentials() {
        LoginUseCase useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);
        User user = User.reconstitute(1L, "jan@example.com", "hashed", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("rawPassword123", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResult result = useCase.execute(new LoginCommand("jan@example.com", "rawPassword123"));

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void execute_recordsLastLoginTimestamp_onSuccessfulLogin() {
        LoginUseCase useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);
        User user = User.reconstitute(1L, "jan@example.com", "hashed", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("rawPassword123", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new LoginCommand("jan@example.com", "rawPassword123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getLastLoginAt()).isNotNull();
    }

    @Test
    void execute_throwsInvalidCredentials_whenEmailUnknown() {
        LoginUseCase useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);

        when(userRepository.findByEmail("nieznany@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LoginCommand("nieznany@example.com", "cokolwiek")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_throwsInvalidCredentials_whenPasswordDoesNotMatch() {
        LoginUseCase useCase = new LoginUseCase(userRepository, passwordHasher, tokenProvider);
        User user = User.reconstitute(1L, "jan@example.com", "hashed", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("zlehaslo", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new LoginCommand("jan@example.com", "zlehaslo")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
