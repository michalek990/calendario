package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import com.calendario.hrnest.domain.facility.exception.FacilityNotFoundException;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import com.calendario.hrnest.domain.user.exception.InvalidSupervisorAssignmentException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserOrganizationUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UpdateUserOrganizationUseCase useCase() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        return new UpdateUserOrganizationUseCase(userRepository, facilityRepository, currentUserProvider, assembler);
    }

    @Test
    void execute_updatesOrganization_whenCallerIsAdmin() {
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());
        User supervisor = User.reconstitute(1L, "szef@example.com", "hash", "Ala", "Szefowa", Role.MANAGER,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findById(1L)).thenReturn(Optional.of(supervisor));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(facilityRepository.existsByName("Warszawa")).thenReturn(true);

        UserProfileView view = useCase().execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Warszawa", 1L));

        assertThat(view.position()).isEqualTo("Programista");
        assertThat(view.department()).isEqualTo("IT");
        assertThat(view.facility()).isEqualTo("Warszawa");
        assertThat(view.supervisorId()).isEqualTo(1L);
        assertThat(view.supervisorFullName()).isEqualTo("Ala Szefowa");
    }

    @Test
    void execute_updatesOrganization_whenHrEditsSomeoneInOwnFacility() {
        User hr = User.reconstitute(1L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                null, null, "Warszawa", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(facilityRepository.existsByName("Warszawa")).thenReturn(true);

        UserProfileView view = useCase().execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Warszawa", null));

        assertThat(view.position()).isEqualTo("Programista");
        assertThat(view.facility()).isEqualTo("Warszawa");
    }

    @Test
    void execute_updatesOrganization_whenHrOnboardsUnassignedUserIntoOwnFacility() {
        User hr = User.reconstitute(1L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(facilityRepository.existsByName("Warszawa")).thenReturn(true);

        UserProfileView view = useCase().execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Warszawa", null));

        assertThat(view.facility()).isEqualTo("Warszawa");
    }

    @Test
    void execute_throwsForbidden_whenHrEditsSomeoneOutsideOwnFacility() {
        User hr = User.reconstitute(1L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                null, null, "Krakow", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(facilityRepository.existsByName("Krakow")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Krakow", null)))
                .isInstanceOf(ForbiddenUserActionException.class);
    }

    @Test
    void execute_throwsForbidden_whenHrTriesToMoveUserToDifferentFacility() {
        User hr = User.reconstitute(1L, "hr@example.com", "hash", "Ala", "Kadrowa", Role.HR,
                null, null, "Warszawa", null, Instant.now());
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                null, null, "Warszawa", null, Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(facilityRepository.existsByName("Krakow")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Krakow", null)))
                .isInstanceOf(ForbiddenUserActionException.class);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsNotHrAdmin() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);

        assertThatThrownBy(() -> useCase().execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", null)))
                .isInstanceOf(ForbiddenUserActionException.class);
    }

    @Test
    void execute_throwsUserNotFound_whenTargetDoesNotExist() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateUserOrganizationCommand(404L, "X", "Y", "Z", null)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void execute_throwsUserNotFound_whenSupervisorDoesNotExist() {
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", 999L)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void execute_throwsInvalidSupervisorAssignment_whenSelfSupervision() {
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(facilityRepository.existsByName("Z")).thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", 2L)))
                .isInstanceOf(InvalidSupervisorAssignmentException.class);
    }

    @Test
    void execute_throwsFacilityNotFound_whenFacilityNameUnknown() {
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(facilityRepository.existsByName("Nieznany")).thenReturn(false);

        assertThatThrownBy(() -> useCase().execute(
                new UpdateUserOrganizationCommand(2L, "X", "Y", "Nieznany", null)))
                .isInstanceOf(FacilityNotFoundException.class);
    }
}
