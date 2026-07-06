package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMyProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_returnsProfile_withSupervisorNameAndIsSupervisorFlag() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        GetMyProfileUseCase useCase = new GetMyProfileUseCase(userRepository, currentUserProvider, assembler);

        User supervisor = User.reconstitute(1L, "szef@example.com", "hash", "Ala", "Szefowa", Role.MANAGER,
                Instant.now());
        User user = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                "Programista", "IT", "Warszawa", 1L, Instant.now());

        when(currentUserProvider.currentUserId()).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(supervisor));
        when(userRepository.existsBySupervisorId(2L)).thenReturn(false);

        UserProfileView view = useCase.execute();

        assertThat(view.position()).isEqualTo("Programista");
        assertThat(view.department()).isEqualTo("IT");
        assertThat(view.facility()).isEqualTo("Warszawa");
        assertThat(view.hasSupervisor()).isTrue();
        assertThat(view.supervisorId()).isEqualTo(1L);
        assertThat(view.supervisorFullName()).isEqualTo("Ala Szefowa");
        assertThat(view.isSupervisor()).isFalse();
    }

    @Test
    void execute_marksIsSupervisor_whenSomeoneReportsToUser() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        GetMyProfileUseCase useCase = new GetMyProfileUseCase(userRepository, currentUserProvider, assembler);

        User manager = User.reconstitute(1L, "szef@example.com", "hash", "Ala", "Szefowa", Role.MANAGER,
                Instant.now());

        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userRepository.existsBySupervisorId(1L)).thenReturn(true);

        UserProfileView view = useCase.execute();

        assertThat(view.isSupervisor()).isTrue();
        assertThat(view.hasSupervisor()).isFalse();
        assertThat(view.supervisorFullName()).isNull();
    }

    @Test
    void execute_throwsUserNotFound_whenCurrentUserMissing() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        GetMyProfileUseCase useCase = new GetMyProfileUseCase(userRepository, currentUserProvider, assembler);

        when(currentUserProvider.currentUserId()).thenReturn(404L);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(useCase::execute).isInstanceOf(UserNotFoundException.class);
    }
}
