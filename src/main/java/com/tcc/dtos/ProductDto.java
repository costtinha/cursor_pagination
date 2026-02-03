package com.tcc.dtos;


import jakarta.validation.constraints.NotBlank;

public record ProductDto(
        @NotBlank(message = "Product name required") String name,
        int scale,
        String pdtDescription,
        int qntyInStock,
        @NotBlank(message = "Buy price required") int buyPrice,
        Integer productLine) {
}
