package com.calendario.hrnest.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-signing-min-32-bytes-long";

    private final JwtService jwtService = new JwtService(SECRET, 3600000L);

    @Test
    void generateToken_thenExtractUsername_returnsOriginalUsername() {
        UserDetails userDetails = user("jan.kowalski@example.com");

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jan.kowalski@example.com");
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        UserDetails userDetails = user("jan.kowalski@example.com");
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forDifferentUser() {
        UserDetails owner = user("jan.kowalski@example.com");
        UserDetails other = user("inna.osoba@example.com");
        String token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(SECRET, 1L);
        UserDetails userDetails = user("jan.kowalski@example.com");
        String token = shortLivedJwtService.generateToken(userDetails);

        Thread.sleep(10);

        assertThat(shortLivedJwtService.isTokenValid(token, userDetails)).isFalse();
    }

    private UserDetails user(String email) {
        return new User(email, "password", java.util.List.of());
    }
}
