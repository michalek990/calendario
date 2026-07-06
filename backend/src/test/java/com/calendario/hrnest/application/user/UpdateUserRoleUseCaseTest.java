package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenRoleChangeException;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserRoleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UpdateUserRoleUseCase useCase() {
        return new UpdateUserRoleUseCase(userRepository, currentUserProvider, new UserProfileAssembler(userRepository));
    }

    @Test
    void execute_changesRole_whenCallerIsAdmin() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        User target = User.reconstitute(2L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE,
                Instant.now());
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileView view = useCase().execute(new UpdateUserRoleCommand(2L, Role.MANAGER));

        assertThat(view.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);

        assertThatThrownBy(() -> useCase().execute(new UpdateUserRoleCommand(2L, Role.MANAGER)))
                .isInstanceOf(ForbiddenRoleChangeException.class);
    }

    @Test
    void execute_throwsUserNotFound_whenTargetDoesNotExist() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(new UpdateUserRoleCommand(404L, Role.MANAGER)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
