package com.jdr.payment.ports.inbound;

import com.jdr.payment.domain.models.Payment;

public interface PaymentValidatorStrategy {
    void validate(Payment payment);
}