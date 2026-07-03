package com.calendario.hrnest.application.auth;

/** Port (strategy) — implementacja (np. BCrypt) w warstwie infrastructure. */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
