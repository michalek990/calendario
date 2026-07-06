package com.calendario.hrnest.application.leave;

import com.calendario.hrnest.domain.leave.exception.ForbiddenLeaveActionException;
import com.calendario.hrnest.domain.user.User;
import com.calendario.hrnest.domain.user.UserRepository;

/**
 * Wspólna reguła autoryzacyjna dla modułu Leave: MANAGER (przełożony) może
 * zatwierdzać/odrzucać/widzieć w kolejce tylko wnioski swoich bezpośrednich
 * podwładnych (relacja przez {@code User.supervisorId}). HR i ADMIN nie
 * podlegają temu ograniczeniu — sprawdzane osobno przez wywołujących.
 */
final class LeaveRequestScopeGuard {

    private LeaveRequestScopeGuard() {
    }

    static void requireDirectSupervisorOf(UserRepository userRepository, Long supervisorId, Long requesterId) {
        if (!isDirectSupervisor(userRepository, supervisorId, requesterId)) {
            throw new ForbiddenLeaveActionException();
        }
    }

    static boolean isDirectSupervisor(UserRepository userRepository, Long supervisorId, Long requesterId) {
        return userRepository.findById(requesterId)
                .map(User::getSupervisorId)
                .map(supervisorId::equals)
                .orElse(false);
    }
}
