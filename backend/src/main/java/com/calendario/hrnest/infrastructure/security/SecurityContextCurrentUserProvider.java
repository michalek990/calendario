package com.calendario.hrnest.infrastructure.security;

import com.calendario.hrnest.application.common.CurrentUserProvider;
import com.calendario.hrnest.domain.user.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long currentUserId() {
        return principal().getUser().getId();
    }

    @Override
    public String currentUserEmail() {
        return principal().getUser().getEmail();
    }

    @Override
    public Role currentUserRole() {
        return principal().getUser().getRole();
    }

    private UserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new IllegalStateException("Brak uwierzytelnionego użytkownika w kontekście bezpieczeństwa");
        }
        return userPrincipal;
    }
}
