package com.calendario.hrnest.application.user;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.application.common.FacilityScope;
import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.ForbiddenUserActionException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Lista wszystkich użytkowników — dla mini panelu administracyjnego. `ADMIN`
 * widzi całą firmę; `HR` widzi wyłącznie pracowników swojego zakładu (patrz
 * {@link FacilityScope}) — jeśli sam nie ma przypisanego zakładu, widzi tylko
 * innych pracowników bez zakładu.
 */
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

        var users = userRepository.findAll().stream();

        if (role == Role.HR) {
            String hrFacility = FacilityScope.facilityOf(userRepository, currentUserProvider.currentUserId());
            users = users.filter(candidate -> Objects.equals(candidate.getFacility(), hrFacility));
        }

        return users
                .map(userProfileAssembler::toView)
                .sorted(Comparator.comparing(UserProfileView::lastName).thenComparing(UserProfileView::firstName))
                .toList();
    }
}
