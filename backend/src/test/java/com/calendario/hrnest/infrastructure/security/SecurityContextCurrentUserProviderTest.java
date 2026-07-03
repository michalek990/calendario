package com.calendario.hrnest.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextCurrentUserProviderTest {

    private final SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsIdEmailAndRole_ofAuthenticatedUser() {
        User user = User.reconstitute(42L, "jan@example.com", "hash", "Jan", "Kowalski", Role.MANAGER, Instant.now());
        authenticateAs(user);

        assertThat(provider.currentUserId()).isEqualTo(42L);
        assertThat(provider.currentUserEmail()).isEqualTo("jan@example.com");
        assertThat(provider.currentUserRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void throwsIllegalState_whenNoAuthenticatedUser() {
        assertThatThrownBy(provider::currentUserId).isInstanceOf(IllegalStateException.class);
    }

    private void authenticateAs(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
