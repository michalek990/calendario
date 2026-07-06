package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lista wszystkich użytkowników — dla mini panelu administracyjnego (HR/ADMIN). */
@Component
public class ListAllUsersUseCase {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileAssembler userProfileAssembler;

    public ListAllUsersUseCase(UserRepository userRepository, CurrentUserProvider currentUserProvider,
                                UserProfileAssembler userProfileAssembler) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.userProfileAssembler = userProfileAssembler;
    }

    public List<UserProfileView> execute() {
        Role role = currentUserProvider.currentUserRole();
        if (role != Role.HR && role != Role.ADMIN) {
            throw new ForbiddenUserActionException();
        }

        return userRepository.findAll().stream()
                .map(userProfileAssembler::toView)
                .sorted(Comparator.comparing(UserProfileView::lastName).thenComparing(UserProfileView::firstName))
                .toList();
    }
}
