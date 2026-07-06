package com.calendario.hrnest.api.user;

public record UpdateUserProfileRequest(
        String position,
        String department,
        String facility,
        Long supervisorId
) {
}
