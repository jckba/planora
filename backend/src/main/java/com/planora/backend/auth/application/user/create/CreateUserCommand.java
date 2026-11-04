package com.planora.backend.auth.application.user.create;

public record CreateUserCommand(
    String firstName,
    String lastName,
    String email,
    String password
) {
}
