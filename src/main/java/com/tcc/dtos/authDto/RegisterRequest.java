package com.tcc.dtos.authDto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "username required")
        String username,
        @NotBlank(message = "password required")
        String password) {
}
