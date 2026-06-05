package com.jdr.payment.infrastructure.adapters.outbound.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El nombre y URL se manejan dinámicamente o se apuntan a una propiedad
@FeignClient(name = "anti-fraud-external-service", url = "${external.api.anti-fraud.url:http://localhost:9090}")
public interface AntiFraudFeignClient {

    // Cambia el tipo de retorno por un DTO o un String según responda el tercero.
    // Para simplificar el caso de uso, simulamos que responde un JSON mapeado a este Record local.
    @GetMapping("/api/v1/fraud-check/{transactionId}")
    FraudCheckResponse checkTransaction(@PathVariable("transactionId") String transactionId);

    // Registro auxiliar para estructurar la respuesta del proveedor externo
    record FraudCheckResponse(String transactionId, String riskLevel) {}
}