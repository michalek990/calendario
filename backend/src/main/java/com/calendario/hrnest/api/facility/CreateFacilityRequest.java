package com.calendario.hrnest.api.facility;

import jakarta.validation.constraints.NotBlank;

public record CreateFacilityRequest(
        @NotBlank String name
) {
}
