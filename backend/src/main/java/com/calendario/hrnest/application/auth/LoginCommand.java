package com.calendario.hrnest.application.auth;

public record LoginCommand(String email, String rawPassword) {
}
