package com.tcc.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OfficeDto(
        @NotBlank(message = "Office name cannot be empty")
        @Size(min = 3, max = 100, message = "Office name must be betweeen 3 and 100 characters")
        String officeName,
        @NotBlank(message = "Email is required")
        @Email(message = "invalid email format")
        String email,
        String phone) {
}
