package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ListAllUsersUseCase useCase() {
        return new ListAllUsersUseCase(userRepository, currentUserProvider, new UserProfileAssembler(userRepository));
    }

    @Test
    void execute_returnsUsers_sortedByLastNameThenFirstName_whenCallerIsHr() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.HR);
        User zbigniew = User.reconstitute(1L, "z@example.com", "hash", "Zbigniew", "Adamski", Role.EMPLOYEE,
                Instant.now());
        User anna = User.reconstitute(2L, "a@example.com", "hash", "Anna", "Baran", Role.EMPLOYEE, Instant.now());
        when(userRepository.findAll()).thenReturn(List.of(zbigniew, anna));

        List<UserProfileView> result = useCase().execute();

        assertThat(result).extracting(UserProfileView::lastName).containsExactly("Adamski", "Baran");
    }

    @Test
    void execute_allowsAdmin() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(useCase().execute()).isEmpty();
    }

    @Test
    void execute_throwsForbidden_whenCallerIsEmployee() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.EMPLOYEE);

        assertThatThrownBy(() -> useCase().execute()).isInstanceOf(ForbiddenUserActionException.class);
    }

    @Test
    void execute_throwsForbidden_whenCallerIsManager() {
        when(currentUserProvider.currentUserRole()).thenReturn(Role.MANAGER);

        assertThatThrownBy(() -> useCase().execute()).isInstanceOf(ForbiddenUserActionException.class);
    }
}
