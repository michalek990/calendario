package com.calendario.hrnest.application.auth;

public record RegisterCommand(String email, String rawPassword, String firstName, String lastName) {
}
