package com.calendario.hrnest.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków") String password,
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}
