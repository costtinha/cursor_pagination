package com.tcc.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

public record OrderDto(
        @NotBlank(message = "Must have a customer id") int customerId,
        @PastOrPresent String orderDate,
        @FutureOrPresent String requiredDate,
        String commments) {
}
