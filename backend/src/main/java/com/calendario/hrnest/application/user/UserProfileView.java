package com.calendario.hrnest.application.user;

import com.calendario.hrnest.domain.user.Role;

public record UserProfileView(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        String position,
        String department,
        String facility,
        boolean isSupervisor,
        boolean hasSupervisor,
        Long supervisorId,
        String supervisorFullName
) {
}
