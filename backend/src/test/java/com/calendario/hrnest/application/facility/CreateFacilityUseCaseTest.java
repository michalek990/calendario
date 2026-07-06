package com.calendario.hrnest.application.facility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.DuplicateFacilityNameException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateFacilityUseCaseTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private CreateFacilityUseCase useCase() {
        return new CreateFacilityUseCase(facilityRepository, currentUserProvider);
    }

    @Test
    void execute_createsFacility_whenCallerIsAdmin() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.existsByName("Warszawa")).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacilityView view = useCase().execute(new CreateFacilityCommand("Warszawa"));

        assertThat(view.name()).isEqualTo("Warszawa");
    }

    @Test
    void execute_throwsForbidden_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);

        assertThatThrownBy(() -> useCase().execute(new CreateFacilityCommand("Warszawa")))
                .isInstanceOf(ForbiddenFacilityActionException.class);
    }

    @Test
    void execute_throwsDuplicateName_whenFacilityAlreadyExists() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.existsByName("Warszawa")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(new CreateFacilityCommand("Warszawa")))
                .isInstanceOf(DuplicateFacilityNameException.class);
    }
}
