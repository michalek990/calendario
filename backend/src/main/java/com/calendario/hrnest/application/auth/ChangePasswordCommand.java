package com.calendario.hrnest.application.auth;

public record ChangePasswordCommand(String currentPassword, String newPassword) {
}
