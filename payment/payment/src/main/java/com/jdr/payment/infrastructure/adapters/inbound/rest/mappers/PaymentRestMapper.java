package com.jdr.payment.infrastructure.adapters.inbound.rest.mappers;

import java.time.LocalDateTime;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentRequest;
import com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentResponse;

public final class PaymentRestMapper {

    private PaymentRestMapper() {}

    public static Payment toDomain(PaymentRequest request) {
        if (request == null) return null;
        return new Payment(
                request.transactionId(),
                request.customerId(),
                request.merchantId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                "PENDING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static PaymentResponse toResponse(AuthorizationResult result) {
        if (result == null) return null;
        return new PaymentResponse(
                result.transactionId(),
                result.status(),
                result.authorizationCode(),
                result.message()
        );
    }
}