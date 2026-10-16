package com.localplatform.servicea.dto;

import com.localplatform.servicea.domain.Order;
import com.localplatform.servicea.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
            o.getId(),
            o.getCustomerId(),
            o.getStatus(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            o.getUpdatedAt()
        );
    }
}
