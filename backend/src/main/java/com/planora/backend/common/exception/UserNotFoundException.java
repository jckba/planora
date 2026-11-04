package com.planora.backend.common.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID message) {
        super("No se ha encontrado al usuario:" + message);
    }
}
