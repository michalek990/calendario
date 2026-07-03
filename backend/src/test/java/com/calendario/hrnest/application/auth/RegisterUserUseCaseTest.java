package com.calendario.hrnest.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    void execute_savesHashedUserAndReturnsToken() {
        RegisterUserUseCase useCase = new RegisterUserUseCase(userRepository, passwordHasher, tokenProvider);
        RegisterCommand command = new RegisterCommand("jan@example.com", "rawPassword123", "Jan", "Kowalski");

        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResult result = useCase.execute(command);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void execute_throwsEmailAlreadyExists_whenEmailTaken() {
        RegisterUserUseCase useCase = new RegisterUserUseCase(userRepository, passwordHasher, tokenProvider);
        RegisterCommand command = new RegisterCommand("zajety@example.com", "rawPassword123", "A", "B");

        when(userRepository.existsByEmail("zajety@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
