package com.localplatform.servicea.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID eventId,
    UUID orderId,
    UUID customerId,
    BigDecimal totalAmount,
    Instant occurredAt
) {
    public static OrderCreatedEvent of(UUID orderId, UUID customerId, BigDecimal totalAmount) {
        return new OrderCreatedEvent(
            UUID.randomUUID(),
            orderId,
            customerId,
            totalAmount,
            Instant.now()
        );
    }
}
