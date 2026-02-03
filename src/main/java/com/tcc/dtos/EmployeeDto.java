package com.tcc.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeeDto(
        @NotBlank(message = "Must have a employee name ")
        String employeeName,
        String phone,
        @NotBlank
        @Email(message = "Wrong email format")
        String email,
        int reportsTo,
        @NotBlank(message = "Office id required") int office) {
}
