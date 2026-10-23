package com.planora.backend.auth.application.user;

public record CreateUserCommand(
    String firstName,
    String lastName,
    String email,
    String password
) {
}
