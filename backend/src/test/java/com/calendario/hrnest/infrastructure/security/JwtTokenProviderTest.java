package com.calendario.hrnest.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.calendario.hrnest.domain.user.Role;
import com.calendario.hrnest.domain.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-signing-min-32-bytes-long";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(SECRET, 3600000L);

    @Test
    void generateToken_thenExtractUsername_returnsUserEmail() {
        User user = user("jan.kowalski@example.com");

        String token = tokenProvider.generateToken(user);

        assertThat(tokenProvider.extractUsername(token)).isEqualTo("jan.kowalski@example.com");
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        User user = user("jan.kowalski@example.com");
        String token = tokenProvider.generateToken(user);

        assertThat(tokenProvider.isTokenValid(token, "jan.kowalski@example.com")).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forDifferentUsername() {
        User user = user("jan.kowalski@example.com");
        String token = tokenProvider.generateToken(user);

        assertThat(tokenProvider.isTokenValid(token, "inna.osoba@example.com")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L);
        User user = user("jan.kowalski@example.com");
        String token = shortLived.generateToken(user);

        Thread.sleep(10);

        assertThat(shortLived.isTokenValid(token, "jan.kowalski@example.com")).isFalse();
    }

    @Test
    void generateToken_includesRoleClaim() {
        User manager = User.reconstitute(1L, "manager@example.com", "hashed", "Ala", "Manager", Role.MANAGER, Instant.now());

        String token = tokenProvider.generateToken(manager);

        String role = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        assertThat(role).isEqualTo("MANAGER");
    }

    private User user(String email) {
        return User.reconstitute(1L, email, "hashed", "Jan", "Kowalski", Role.EMPLOYEE, Instant.now());
    }
}
