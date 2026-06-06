package com.jdr.payment.ports.inbound;

import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.domain.models.AuthorizationResult;

public interface AuthorizePaymentInputPort {
    AuthorizationResult execute(Payment payment);
}