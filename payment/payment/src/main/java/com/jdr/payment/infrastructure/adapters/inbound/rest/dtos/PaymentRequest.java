package com.jdr.payment.infrastructure.adapters.inbound.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequest(
    @NotBlank(message = "transactionId is required")
    String transactionId,
    
    @NotBlank(message = "customerId is required")
    String customerId,
    
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    BigDecimal amount,
    
    @NotBlank(message = "currency is required")
    String currency,
    
    @NotBlank(message = "merchantId is required")
    String merchantId,
    
    @NotBlank(message = "paymentMethod is required")
    String paymentMethod
) {}