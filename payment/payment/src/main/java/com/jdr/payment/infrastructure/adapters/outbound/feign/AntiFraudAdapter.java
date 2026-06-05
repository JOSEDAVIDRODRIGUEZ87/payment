package com.jdr.payment.infrastructure.adapters.outbound.feign;

import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AntiFraudAdapter implements AntiFraudClientPort {

    private final AntiFraudFeignClient feignClient;

    @Override
    // Vincula este método al Circuit Breaker configurado en el application.yml
    @CircuitBreaker(name = "antiFraudService", fallbackMethod = "fallbackCheckRiskStatus")
    public String checkRiskStatus(String transactionId) {
        log.info("[HTTP Request] Consultando proveedor externo antifraude para la transaccion: {}", transactionId);
        
        AntiFraudFeignClient.FraudCheckResponse response = feignClient.checkTransaction(transactionId);
        
        log.info("[HTTP Response] Proveedor respondió riesgo: {} para transaccion: {}", response.riskLevel(), transactionId);
        return response.riskLevel();
    }

    /**
     * Método Fallback que se ejecutará de forma automática si:
     * 1. El proveedor externo responde un error HTTP (5xx, 4xx).
     * 2. Se alcanza el Timeout configurado (ej. tarda más de 2 segundos).
     * 3. El Circuit Breaker está en estado ABIERTO debido a fallas consecutivas.
     */
    public String fallbackCheckRiskStatus(String transactionId, Throwable exception) {
        log.error("[Resilience4j Fallback] Activado para transaccion: {}. Motivo del fallo: {}", 
                transactionId, exception.getMessage());
        
        // REQUERIMIENTO SENIOR DE NEGOCIO: Ante caídas del sistema de prevención de fraude, 
        // la regla financiera más segura (estrategia conservadora) es asumir HIGH_RISK 
        // para proteger el dinero del comercio, forzando un rechazo controlado.
        return "HIGH_RISK";
    }
}