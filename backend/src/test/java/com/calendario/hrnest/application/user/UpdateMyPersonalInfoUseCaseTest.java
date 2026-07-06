package com.calendario.hrnest.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.InvalidBirthDateException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMyPersonalInfoUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Test
    void execute_updatesOwnPersonalInfo() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateMyPersonalInfoUseCase useCase =
                new UpdateMyPersonalInfoUseCase(userRepository, currentUserProvider, assembler);

        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsBySupervisorId(1L)).thenReturn(false);

        UserProfileView view = useCase.execute(
                new UpdatePersonalInfoCommand(LocalDate.of(1990, 5, 1), "+48123456789", "https://example.com/a.png"));

        assertThat(view.birthDate()).isEqualTo(LocalDate.of(1990, 5, 1));
        assertThat(view.phoneNumber()).isEqualTo("+48123456789");
        assertThat(view.avatarUrl()).isEqualTo("https://example.com/a.png");
    }

    @Test
    void execute_throwsInvalidBirthDate_whenInTheFuture() {
        UserProfileAssembler assembler = new UserProfileAssembler(userRepository);
        UpdateMyPersonalInfoUseCase useCase =
                new UpdateMyPersonalInfoUseCase(userRepository, currentUserProvider, assembler);

        User user = User.reconstitute(1L, "jan@example.com", "hash", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());

        when(currentUserProvider.currentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.execute(
                new UpdatePersonalInfoCommand(LocalDate.now().plusDays(1), null, null)))
                .isInstanceOf(InvalidBirthDateException.class);
    }
}
