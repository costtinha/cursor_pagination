package com.tcc.dtos;

import jakarta.validation.constraints.NotBlank;

public record ProductLineDto(
        @NotBlank(message = "description in text required")
        String descInText,
        String descInHtml,
        String image) {
}
