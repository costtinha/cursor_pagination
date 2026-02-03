package com.tcc.dtos.authDto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "must have an username") String username,
        @NotBlank(message = "must have an password") String password) {
}
