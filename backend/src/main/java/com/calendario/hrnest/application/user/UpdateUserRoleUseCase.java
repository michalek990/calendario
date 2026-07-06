package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenRoleChangeException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Component;

/** Zmiana roli użytkownika — wyłącznie ADMIN (bardziej wrażliwe niż dane organizacyjne, stąd węższy dostęp). */
@Component
public class UpdateUserRoleUseCase {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileAssembler userProfileAssembler;

    public UpdateUserRoleUseCase(UserRepository userRepository, CurrentUserProvider currentUserProvider,
                                  UserProfileAssembler userProfileAssembler) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.userProfileAssembler = userProfileAssembler;
    }

    public UserProfileView execute(UpdateUserRoleCommand command) {
        if (currentUserProvider.currentUserRole() != Role.ADMIN) {
            throw new ForbiddenRoleChangeException();
        }

        User user = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

        return userProfileAssembler.toView(userRepository.save(user.updateRole(command.newRole())));
    }
}
