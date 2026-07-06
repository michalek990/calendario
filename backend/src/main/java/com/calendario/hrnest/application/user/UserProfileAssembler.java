package com.calendario.hrnest.application.user;

import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Buduje {@link UserProfileView} z agregatu {@link User}, dociągając dane
 * pochodne (przełożony, czy użytkownik sam jest przełożonym) z repozytorium.
 * Współdzielone przez use case'y odczytu i aktualizacji profilu.
 */
@Component
class UserProfileAssembler {

    private final UserRepository userRepository;

    UserProfileAssembler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    UserProfileView toView(User user) {
        String supervisorFullName = null;
        if (user.getSupervisorId() != null) {
            supervisorFullName = userRepository.findById(user.getSupervisorId())
                    .map(supervisor -> supervisor.getFirstName() + " " + supervisor.getLastName())
                    .orElse(null);
        }

        boolean isSupervisor = userRepository.existsBySupervisorId(user.getId());

        return new UserProfileView(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getPosition(),
                user.getDepartment(),
                user.getFacility(),
                isSupervisor,
                user.getSupervisorId() != null,
                user.getSupervisorId(),
                supervisorFullName,
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getLastLoginAt());
    }
}
