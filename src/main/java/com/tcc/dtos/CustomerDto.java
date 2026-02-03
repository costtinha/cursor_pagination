package com.tcc.dtos;

import com.tcc.entity.Employee;
import jakarta.validation.constraints.NotBlank;

public record CustomerDto(
        @NotBlank(message = "Must have a sales representative employer num.") int salesRepEmployee,
        String name,
        String phone,
        String state,
        @NotBlank(message = "Must have a postal code") String postalCode,
        String country) {
}
