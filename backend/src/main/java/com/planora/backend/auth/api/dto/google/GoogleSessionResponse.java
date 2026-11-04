package com.planora.backend.auth.api.dto.google;

import com.planora.backend.auth.api.dto.login.LoginResponse;

public record GoogleSessionResponse(
    String status,
    String email,
    String firstName,
    String lastName,
    LoginResponse login
) {
}
