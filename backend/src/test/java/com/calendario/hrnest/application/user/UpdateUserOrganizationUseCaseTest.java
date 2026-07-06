package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
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
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_updatesOrganization_whenCallerIsHrAdmin() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateUserOrganizationUseCase useCase =
                new UpdateUserOrganizationUseCase(userRepository, currentUserProvider, assembler);

        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());
        User supervisor = User.reconstitute(1L, "szef@example.com", "hash", "Ala", "Szefowa", Role.MANAGER,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR_ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findById(1L)).thenReturn(Optional.of(supervisor));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsBySupervisorId(2L)).thenReturn(false);

        UserProfileView view = useCase.execute(
                new UpdateUserOrganizationCommand(2L, "Programista", "IT", "Warszawa", 1L));

        assertThat(view.position()).isEqualTo("Programista");
        assertThat(view.department()).isEqualTo("IT");
        assertThat(view.facility()).isEqualTo("Warszawa");
        assertThat(view.supervisorId()).isEqualTo(1L);
        assertThat(view.supervisorFullName()).isEqualTo("Ala Szefowa");
    }

    @Test
    void execute_throwsForbidden_whenCallerIsNotHrAdmin() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateUserOrganizationUseCase useCase =
                new UpdateUserOrganizationUseCase(userRepository, currentUserProvider, assembler);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);

        assertThatThrownBy(() -> useCase.execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", null)))
                .isInstanceOf(ForbiddenUserActionException.class);
    }

    @Test
    void execute_throwsUserNotFound_whenTargetDoesNotExist() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateUserOrganizationUseCase useCase =
                new UpdateUserOrganizationUseCase(userRepository, currentUserProvider, assembler);

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR_ADMIN);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateUserOrganizationCommand(404L, "X", "Y", "Z", null)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void execute_throwsUserNotFound_whenSupervisorDoesNotExist() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateUserOrganizationUseCase useCase =
                new UpdateUserOrganizationUseCase(userRepository, currentUserProvider, assembler);

        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR_ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", 999L)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void execute_throwsInvalidSupervisorAssignment_whenSelfSupervision() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateUserOrganizationUseCase useCase =
                new UpdateUserOrganizationUseCase(userRepository, currentUserProvider, assembler);

        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());

        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR_ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> useCase.execute(new UpdateUserOrganizationCommand(2L, "X", "Y", "Z", 2L)))
                .isInstanceOf(InvalidSupervisorAssignmentException.class);
    }
}
