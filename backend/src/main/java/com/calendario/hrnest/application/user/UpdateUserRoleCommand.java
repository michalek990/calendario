package com.calendario.hrnest.application.user;

import com.calendario.hrnest.domain.user.Role;

public record UpdateUserRoleCommand(Long targetUserId, Role newRole) {
}
