package com.calendario.hrnest.application.facility;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.DuplicateFacilityNameException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import org.springframework.stereotype.Component;

/** Tworzenie nowego zakładu (instytucji) — wyłącznie ADMIN. */
@Component
public class CreateFacilityUseCase {

    private final FacilityRepository facilityRepository;
    private final CurrentUserProvider currentUserProvider;

    public CreateFacilityUseCase(FacilityRepository facilityRepository, CurrentUserProvider currentUserProvider) {
        this.facilityRepository = facilityRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public FacilityView execute(CreateFacilityCommand command) {
        if (currentUserProvider.currentUserRole() != Role.ADMIN) {
            throw new ForbiddenFacilityActionException();
        }

        if (facilityRepository.existsByName(command.name())) {
            throw new DuplicateFacilityNameException(command.name());
        }

        Facility saved = facilityRepository.save(Facility.create(command.name()));

        return FacilityView.from(saved);
    }
}
