package com.calendario.hrnest.application.user;

import java.time.LocalDate;

public record UpdatePersonalInfoCommand(
        LocalDate birthDate,
        String phoneNumber,
        String avatarUrl
) {
}
