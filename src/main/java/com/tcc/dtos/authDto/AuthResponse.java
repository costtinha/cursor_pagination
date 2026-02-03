package com.tcc.dtos.authDto;

import jakarta.validation.constraints.NotBlank;

public record AuthResponse(String accessToken, String refreshToken) {
}
