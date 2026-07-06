package com.calendario.hrnest.api.facility;

import jakarta.validation.constraints.NotBlank;

public record UpdateFacilityRequest(
        @NotBlank String name
) {
}
