package com.jdr.payment.infrastructure.adapters.outbound.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;

@FeignClient(name = "anti-fraud-external-service", url = "${external.api.anti-fraud.url:http://localhost:9090}")
public interface AntiFraudFeignClient {

    @PostMapping("/api/antifraud/validate")
    FraudCheckResponse checkTransaction(@RequestBody FraudCheckRequest request);

    // DTO de petición hacia el puerto 9090
    record FraudCheckRequest(String transactionId, String customerId, BigDecimal amount) {}

    // DTO de respuesta del puerto 9090
    record FraudCheckResponse(String riskLevel, String reason) {}
}