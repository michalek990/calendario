package com.calendario.hrnest.application.facility;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.FacilityInUseException;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteFacilityUseCaseTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private DeleteFacilityUseCase useCase() {
        return new DeleteFacilityUseCase(facilityRepository, userRepository, currentUserProvider);
    }

    @Test
    void execute_deletesFacility_whenNoUsersAssigned() {
        Facility facility = Facility.reconstitute(1L, "Warszawa", Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(userRepository.existsByFacility("Warszawa")).thenReturn(false);

        useCase().execute(1L);

        verify(facilityRepository).deleteById(1L);
    }

    @Test
    void execute_throwsInUse_whenUsersStillAssigned() {
        Facility facility = Facility.reconstitute(1L, "Warszawa", Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(userRepository.existsByFacility("Warszawa")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(FacilityInUseException.class);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);

        assertThatThrownBy(() -> useCase().execute(1L)).isInstanceOf(ForbiddenFacilityActionException.class);
    }

    @Test
    void execute_throwsNotFound_whenFacilityDoesNotExist() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(404L)).isInstanceOf(FacilityNotFoundException.class);
    }
}
