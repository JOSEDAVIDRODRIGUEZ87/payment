package com.jdr.anti_fraud_service.dto;

public record AntiFraudResponse(String riskLevel, // "LOW_RISK" o "HIGH_RISK"
		String reason) {
}