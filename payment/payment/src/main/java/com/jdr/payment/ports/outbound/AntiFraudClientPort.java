package com.jdr.payment.ports.outbound;

public interface AntiFraudClientPort {
    String checkRiskStatus(String transactionId);
}