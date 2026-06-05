package com.jdr.anti_fraud_service.dto;

import java.math.BigDecimal;

public record AntiFraudRequest(String transactionId, String customerId, BigDecimal amount) {
}