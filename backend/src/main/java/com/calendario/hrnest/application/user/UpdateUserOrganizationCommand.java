package com.calendario.hrnest.application.user;

public record UpdateUserOrganizationCommand(
        Long targetUserId,
        String position,
        String department,
        String facility,
        Long supervisorId
) {
}
