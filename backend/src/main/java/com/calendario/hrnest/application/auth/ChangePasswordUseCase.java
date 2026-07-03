package com.calendario.hrnest.application.auth;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.InvalidCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentUserProvider currentUserProvider;

    public ChangePasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher,
                                  CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.currentUserProvider = currentUserProvider;
    }

    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findByEmail(currentUserProvider.currentUserEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        userRepository.save(user.changePassword(passwordHasher.hash(command.newPassword())));
    }
}
