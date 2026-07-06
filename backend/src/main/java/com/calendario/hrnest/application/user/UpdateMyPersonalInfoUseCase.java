package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Aktualizacja danych personalnych (data urodzenia, telefon, awatar) —
 * w przeciwieństwie do {@link UpdateUserOrganizationUseCase} może to zrobić
 * każdy użytkownik wyłącznie dla samego siebie (bez roli HR/ADMIN).
 */
@Component
public class UpdateMyPersonalInfoUseCase {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileAssembler userProfileAssembler;

    public UpdateMyPersonalInfoUseCase(UserRepository userRepository, CurrentUserProvider currentUserProvider,
                                        UserProfileAssembler userProfileAssembler) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.userProfileAssembler = userProfileAssembler;
    }

    public UserProfileView execute(UpdatePersonalInfoCommand command) {
        Long userId = currentUserProvider.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        User updated = user.updatePersonalInfo(command.birthDate(), command.phoneNumber(), command.avatarUrl());

        return userProfileAssembler.toView(userRepository.save(updated));
    }
}
