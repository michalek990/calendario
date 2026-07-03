package com.calendario.hrnest.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków") String newPassword
) {
}
