package com.jdr.payment.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
    String transactionId,
    String customerId,
    String merchantId,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // Puedes incluir constructores compactos para lógica de inicialización básica
    public Payment {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
    }
}