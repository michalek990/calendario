package com.calendario.hrnest.application.common;

import com.calendario.hrnest.domain.user.Role;

/**
 * Port — dostęp do tożsamości aktualnie zalogowanego użytkownika, bez
 * zależności use case'ów od Spring Security / SecurityContextHolder.
 */
public interface CurrentUserProvider {

    Long currentUserId();

    String currentUserEmail();

    Role currentUserRole();
}
