package com.calendario.hrnest.api.project;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank String name,
        String description
) {
}
