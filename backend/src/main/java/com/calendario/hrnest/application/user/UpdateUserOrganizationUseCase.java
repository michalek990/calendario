package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.common.FacilityScope;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Aktualizacja stanowiska/działu/zakładu/przełożonego — wyłącznie HR lub
 * ADMIN. `HR` przypisany do zakładu może edytować wyłącznie pracowników
 * swojego zakładu (albo jeszcze nieprzypisanych do żadnego — np. świeżo
 * zatrudnionych) i może ustawić zakład wyłącznie na swój własny, nigdy na
 * inny (patrz {@link FacilityScope}) — `ADMIN` nie podlega temu ograniczeniu.
 */
@Component
public class UpdateUserOrganizationUseCase {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileAssembler userProfileAssembler;

    public UpdateUserOrganizationUseCase(UserRepository userRepository, FacilityRepository facilityRepository,
                                          CurrentUserProvider currentUserProvider,
                                          UserProfileAssembler userProfileAssembler) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
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

        if (command.facility() != null && !facilityRepository.existsByName(command.facility())) {
            throw new FacilityNotFoundException(command.facility());
        }

        if (role == Role.HR) {
            String hrFacility = FacilityScope.facilityOf(userRepository, currentUserProvider.currentUserId());
            boolean targetAlreadyElsewhere = user.getFacility() != null && !Objects.equals(user.getFacility(), hrFacility);
            if (targetAlreadyElsewhere || !Objects.equals(command.facility(), hrFacility)) {
                throw new ForbiddenUserActionException();
            }
        }

        User updated = user.updateOrganization(
                command.position(), command.department(), command.facility(), command.supervisorId());

        return userProfileAssembler.toView(userRepository.save(updated));
    }
}
