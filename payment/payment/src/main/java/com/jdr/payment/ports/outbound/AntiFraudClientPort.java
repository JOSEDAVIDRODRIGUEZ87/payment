package com.jdr.payment.ports.outbound;

import com.jdr.payment.domain.models.Payment;

public interface AntiFraudClientPort {
    // 🛠️ Cambiamos String transactionId por Payment payment
    String checkRiskStatus(Payment payment); 
}