package com.jdr.anti_fraud_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jdr.anti_fraud_service.dto.AntiFraudRequest;
import com.jdr.anti_fraud_service.dto.AntiFraudResponse;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/antifraud")
@Slf4j
public class AntiFraudController {

    @PostMapping("/validate")
    public ResponseEntity<AntiFraudResponse> validateTransaction(@RequestBody AntiFraudRequest request) {
        log.info("[ANTIFRAUD] Recibida solicitud de validación para Tx: {}", request.transactionId());

        // Regla Simulada 1: Si el monto supera el límite (ej. 500,000 COP), es ALTO RIESGO
        BigDecimal limit = new BigDecimal("500000.00");
        if (request.amount().compareTo(limit) > 0) {
            log.warn("[ANTIFRAUD] Tx: {} rechazada de forma preventiva. Monto excede el límite de riesgo.", request.transactionId());
            return ResponseEntity.ok(new AntiFraudResponse("HIGH_RISK", "Transaction amount exceeds risk thresholds"));
        }

        // Regla Simulada 2: Simulación de cliente en lista negra (Para pruebas de HIGH_RISK)
        if ("CUST-999".equals(request.customerId())) {
            log.warn("[ANTIFRAUD] Tx: {} bajo sospecha. Cliente bloqueado en lista negra.", request.transactionId());
            return ResponseEntity.ok(new AntiFraudResponse("HIGH_RISK", "Customer identifier blacklisted"));
        }

        // Flujo Feliz: Riesgo Bajo
        log.info("[ANTIFRAUD] Tx: {} evaluada exitosamente como LOW_RISK", request.transactionId());
        return ResponseEntity.ok(new AntiFraudResponse("LOW_RISK", "No anomalies detected"));
    }
}