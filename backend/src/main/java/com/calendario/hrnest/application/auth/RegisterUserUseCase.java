package com.calendario.hrnest.application.auth;

import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher,
                                TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public AuthResult execute(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        User user = User.register(
                command.email(),
                passwordHasher.hash(command.rawPassword()),
                command.firstName(),
                command.lastName());

        User saved = userRepository.save(user);

        return new AuthResult(tokenProvider.generateToken(saved));
    }
}
