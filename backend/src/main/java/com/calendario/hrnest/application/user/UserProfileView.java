package com.calendario.hrnest.application.user;

import com.calendario.hrnest.domain.user.Role;
import java.time.Instant;
import java.time.LocalDate;

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
        String supervisorFullName,
        LocalDate birthDate,
        String phoneNumber,
        String avatarUrl,
        Instant lastLoginAt
) {
}
