package com.calendario.hrnest.application.common;

import com.calendario.hrnest.domain.user.UserRepository;
import com.calendario.hrnest.domain.user.exception.UserNotFoundException;
import java.util.Objects;

/**
 * Wspólna reguła: `HR` przypisany do zakładu widzi/zarządza wyłącznie tym
 * zakładem (w przeciwieństwie do `ADMIN`, który nie podlega temu
 * ograniczeniu). Używane przez moduły User/Leave/Time Tracking wszędzie tam,
 * gdzie `HR` filtruje albo zatwierdza coś dotyczącego innego pracownika.
 */
public final class FacilityScope {

    private FacilityScope() {
    }

    /** Zakład danego użytkownika (może być {@code null}, jeśli nie ma przypisanego). */
    public static String facilityOf(UserRepository userRepository, Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId))
                .getFacility();
    }

    /** Czy dwaj użytkownicy mają ten sam zakład (w tym: obaj bez zakładu — {@code null == null}). */
    public static boolean isSameFacility(UserRepository userRepository, Long userId1, Long userId2) {
        return Objects.equals(facilityOf(userRepository, userId1), facilityOf(userRepository, userId2));
    }
}
