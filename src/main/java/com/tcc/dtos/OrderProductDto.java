package com.tcc.dtos;


import jakarta.validation.constraints.NotBlank;

public record OrderProductDto(
        @NotBlank(message = "order id required")
        Integer orderId,
        @NotBlank(message = "product id required")
        Integer productId,
        int qnty,
        int priceEach) {
}
