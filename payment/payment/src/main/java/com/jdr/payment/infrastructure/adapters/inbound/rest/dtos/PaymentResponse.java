package com.jdr.payment.infrastructure.adapters.inbound.rest.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Oculta el authorizationCode si viene nulo (Caso rechazado)
public record PaymentResponse(
    String transactionId,
    String status,
    String authorizationCode,
    String message
) {}