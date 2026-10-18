package com.planora.backend.auth.api.dto;

public record GoogleSessionResponse(
    String status,
    String email,
    String firstName,
    String lastName,
    LoginResponse login
) {
}
