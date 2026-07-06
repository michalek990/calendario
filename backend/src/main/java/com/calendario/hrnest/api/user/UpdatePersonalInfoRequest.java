package com.calendario.hrnest.api.user;

import java.time.LocalDate;

public record UpdatePersonalInfoRequest(
        LocalDate birthDate,
        String phoneNumber,
        String avatarUrl
) {
}
