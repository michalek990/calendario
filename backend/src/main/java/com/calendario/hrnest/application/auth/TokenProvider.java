package com.calendario.hrnest.application.auth;

import com.calendario.hrnest.domain.user.User;

/** Port (strategy) — implementacja (np. JWT) w warstwie infrastructure. */
public interface TokenProvider {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, String expectedUsername);
}
