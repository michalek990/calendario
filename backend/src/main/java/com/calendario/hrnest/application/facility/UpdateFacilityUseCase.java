package com.calendario.hrnest.application.facility;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.DuplicateFacilityNameException;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Zmiana nazwy zakładu — wyłącznie ADMIN. Nazwa zakładu jest zwykłym polem
 * tekstowym u pracownika ({@code User.facility}), więc zmiana nazwy tutaj
 * kaskadowo aktualizuje wszystkich pracowników, którzy mieli przypisaną starą
 * nazwę — inaczej zostaliby z "osieroconą" wartością spoza listy zakładów.
 */
@Component
public class UpdateFacilityUseCase {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public UpdateFacilityUseCase(FacilityRepository facilityRepository, UserRepository userRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public FacilityView execute(UpdateFacilityCommand command) {
        if (currentUserProvider.currentUserRole() != Role.ADMIN) {
            throw new ForbiddenFacilityActionException();
        }

        Facility facility = facilityRepository.findById(command.facilityId())
                .orElseThrow(() -> new FacilityNotFoundException(command.facilityId()));

        if (!facility.getName().equals(command.name()) && facilityRepository.existsByName(command.name())) {
            throw new DuplicateFacilityNameException(command.name());
        }

        String previousName = facility.getName();
        Facility renamed = facilityRepository.save(facility.rename(command.name()));

        if (!previousName.equals(command.name())) {
            for (User user : userRepository.findByFacility(previousName)) {
                userRepository.save(user.updateOrganization(
                        user.getPosition(), user.getDepartment(), command.name(), user.getSupervisorId()));
            }
        }

        return FacilityView.from(renamed);
    }
}
