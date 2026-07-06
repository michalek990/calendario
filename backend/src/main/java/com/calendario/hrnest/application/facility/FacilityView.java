package com.calendario.hrnest.application.facility;

import com.calendario.hrnest.domain.facility.Facility;
import java.time.Instant;

public record FacilityView(
        Long id,
        String name,
        Instant createdAt
) {

    public static FacilityView from(Facility facility) {
        return new FacilityView(facility.getId(), facility.getName(), facility.getCreatedAt());
    }
}
