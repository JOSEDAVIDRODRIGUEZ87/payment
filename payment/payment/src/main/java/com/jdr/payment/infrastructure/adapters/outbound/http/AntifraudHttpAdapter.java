package com.jdr.payment.infrastructure.adapters.outbound.http;

import com.jdr.payment.domain.models.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class AntifraudHttpAdapter {

	private final RestClient restClient;

	public AntifraudHttpAdapter() {
		// Apunta directamente al puerto 9090 que acabas de levantar
		this.restClient = RestClient.builder().baseUrl("http://localhost:9090/api/antifraud").build();
	}

	public AntifraudClientResponse checkFraudRisk(Payment payment) {
		log.info("[HTTP ADAPTER] Solicitando validación de riesgo al proveedor externo para Tx: {}",
				payment.transactionId());

		try {
			// Mapeamos al formato que espera tu controlador del puerto 9090
			AntifraudClientRequest request = new AntifraudClientRequest(payment.transactionId(), payment.customerId(),
					payment.amount());

			// POST HTTP Real hacia el servicio satélite
			return restClient.post().uri("/validate").contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
					.body(AntifraudClientResponse.class);

		} catch (Exception e) {
			log.error("[HTTP ADAPTER] Error de comunicación con el servicio antifraude: {}", e.getMessage());
			// Lanzamos la excepción para que sea interceptada por el Circuit Breaker
			// (Resilience4j)
			throw new RuntimeException("External anti-fraud provider unavailable", e);
		}
	}

	// DTOs internos del adaptador para el mapeo de red local
	public record AntifraudClientRequest(String transactionId, String customerId, BigDecimal amount) {
	}

	public record AntifraudClientResponse(String riskLevel, String reason) {
	}
}