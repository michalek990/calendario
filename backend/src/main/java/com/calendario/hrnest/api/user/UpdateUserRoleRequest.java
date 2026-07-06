package com.calendario.hrnest.api.user;

import com.calendario.hrnest.domain.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {
}
