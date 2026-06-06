package com.jdr.payment.infrastructure.adapters.outbound.http;

import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.ports.outbound.AntiFraudClientPort; // 🟢 Importamos tu puerto real
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Primary
@Slf4j
public class AntifraudHttpAdapter implements AntiFraudClientPort { // 🟢 Implementa tu puerto real

    private final RestClient restClient;

    public AntifraudHttpAdapter(@Value("${api.antifraud.base-url:http://localhost:9090/api/antifraud}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override // 🟢 Sobrescribimos el método exacto del puerto: checkRiskStatus
    @CircuitBreaker(name = "antifraudService", fallbackMethod = "fallbackCheckFraudRisk")
    @Retry(name = "antifraudService")
    public String checkRiskStatus(Payment payment) { 
        log.info("[HTTP ADAPTER] Consultando proveedor externo antifraude para Tx: {}", payment.transactionId());

        AntifraudClientRequest request = new AntifraudClientRequest(
                payment.transactionId(), 
                payment.customerId(),
                payment.amount()
        );

        AntifraudClientResponse response = restClient.post()
                .uri("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AntifraudClientResponse.class);

        if (response != null) {
            log.info("[HTTP ADAPTER] Respuesta recibida del proveedor. Nivel de riesgo: {}", response.riskLevel());
            return response.riskLevel(); // Retorna "LOW_RISK" o "HIGH_RISK"
        }

        throw new RuntimeException("Empty response from anti-fraud provider");
    }

    /**
     * 🛡️ Método de Fallback Controlado
     * Mantiene exactamente la misma firma (parámetros y tipo de retorno) del método original más la excepción.
     */
    public String fallbackCheckFraudRisk(Payment payment, Throwable exception) {
        log.warn("[RESILIENCIA] Fallback activado para la Tx: {} debido a falla en el tercero: {}.", 
                payment.transactionId(), exception.getMessage());
        
        // Estrategia Senior: Ante la duda o caída del proveedor de fraude, asumimos HIGH_RISK para proteger el negocio.
        return "HIGH_RISK"; 
    }

    // DTOs internos del adaptador para el mapeo del JSON de red local
    public record AntifraudClientRequest(String transactionId, String customerId, BigDecimal amount) {}
    public record AntifraudClientResponse(String riskLevel, String reason) {}
}