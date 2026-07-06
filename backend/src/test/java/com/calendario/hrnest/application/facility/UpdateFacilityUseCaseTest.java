package com.calendario.hrnest.application.facility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.DuplicateFacilityNameException;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.facility.exception.ForbiddenFacilityActionException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateFacilityUseCaseTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UpdateFacilityUseCase useCase() {
        return new UpdateFacilityUseCase(facilityRepository, userRepository, currentUserProvider);
    }

    @Test
    void execute_renamesFacility_andCascadesToAssignedUsers() {
        Facility facility = Facility.reconstitute(1L, "Warszawa", Instant.now());
        User assigned = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                null, null, "Warszawa", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityRepository.existsByName("Warszawa Centrum")).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByFacility("Warszawa")).thenReturn(List.of(assigned));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacilityView view = useCase().execute(new UpdateFacilityCommand(1L, "Warszawa Centrum"));

        assertThat(view.name()).isEqualTo("Warszawa Centrum");
        verify(userRepository).save(argThatFacilityEquals("Warszawa Centrum"));
    }

    @Test
    void execute_doesNotTouchUsers_whenNameUnchanged() {
        Facility facility = Facility.reconstitute(1L, "Warszawa", Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(new UpdateFacilityCommand(1L, "Warszawa"));

        verify(userRepository, never()).findByFacility(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_throwsForbidden_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);

        assertThatThrownBy(() -> useCase().execute(new UpdateFacilityCommand(1L, "Warszawa")))
                .isInstanceOf(ForbiddenFacilityActionException.class);
    }

    @Test
    void execute_throwsNotFound_whenFacilityDoesNotExist() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateFacilityCommand(404L, "Warszawa")))
                .isInstanceOf(FacilityNotFoundException.class);
    }

    @Test
    void execute_throwsDuplicateName_whenAnotherFacilityAlreadyUsesName() {
        Facility facility = Facility.reconstitute(1L, "Warszawa", Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));
        when(facilityRepository.existsByName("Krakow")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(new UpdateFacilityCommand(1L, "Krakow")))
                .isInstanceOf(DuplicateFacilityNameException.class);
    }

    private static User argThatFacilityEquals(String expectedFacility) {
        return org.mockito.ArgumentMatchers.argThat(user -> expectedFacility.equals(user.getFacility()));
    }
}
