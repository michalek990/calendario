package com.calendario.hrnest.application.facility;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.FacilityInUseException;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import org.springframework.stereotype.Component;

/** Usuwanie zakładu — wyłącznie ADMIN, zablokowane dopóki są do niego przypisani pracownicy. */
@Component
public class DeleteFacilityUseCase {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public DeleteFacilityUseCase(FacilityRepository facilityRepository, UserRepository userRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public void execute(Long facilityId) {
        if (currentUserProvider.currentUserRole() != Role.ADMIN) {
            throw new ForbiddenFacilityActionException();
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        if (userRepository.existsByFacility(facility.getName())) {
            throw new FacilityInUseException(facility.getName());
        }

        facilityRepository.deleteById(facilityId);
    }
}
