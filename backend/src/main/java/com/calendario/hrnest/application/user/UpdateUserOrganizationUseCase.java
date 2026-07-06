package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Component;

/** Aktualizacja stanowiska/działu/zakładu/przełożonego — wyłącznie HR lub ADMIN. */
@Component
public class UpdateUserOrganizationUseCase {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileAssembler userProfileAssembler;

    public UpdateUserOrganizationUseCase(UserRepository userRepository, CurrentUserProvider currentUserProvider,
                                          UserProfileAssembler userProfileAssembler) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.userProfileAssembler = userProfileAssembler;
    }

    public UserProfileView execute(UpdateUserOrganizationCommand command) {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenUserActionException();
        }

        User user = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

        if (command.supervisorId() != null) {
            userRepository.findById(command.supervisorId())
                    .orElseThrow(() -> new UserNotFoundException(command.supervisorId()));
        }

        User updated = user.updateOrganization(
                command.position(), command.department(), command.facility(), command.supervisorId());

        return userProfileAssembler.toView(userRepository.save(updated));
    }
}
