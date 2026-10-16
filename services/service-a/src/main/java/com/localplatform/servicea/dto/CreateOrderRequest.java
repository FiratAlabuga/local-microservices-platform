package com.localplatform.servicea.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull(message = "customerId zorunludur")
    UUID customerId,

    @NotNull(message = "totalAmount zorunludur")
    @DecimalMin(value = "0.01", message = "totalAmount 0.01 veya üzeri olmalıdır")
    BigDecimal totalAmount
) {}
