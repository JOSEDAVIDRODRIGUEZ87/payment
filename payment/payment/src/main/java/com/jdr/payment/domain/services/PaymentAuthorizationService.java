package com.jdr.payment.domain.services;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.jdr.payment.domain.exceptions.BusinessException;
import com.jdr.payment.domain.factory.AuthorizationResultFactory;
import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.ports.inbound.AuthorizePaymentUseCase;
import com.jdr.payment.ports.inbound.GetPaymentUseCase;
import com.jdr.payment.ports.inbound.PaymentValidatorStrategy;
import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; // <-- Importación clave

@Slf4j // Agregamos logs requeridos por la prueba
public class PaymentAuthorizationService implements AuthorizePaymentUseCase, GetPaymentUseCase {

	private final List<PaymentValidatorStrategy> validators;
	private final AntiFraudClientPort antiFraudClient;
	private final PaymentRepositoryPort paymentRepository;
	private final CacheManager cacheManager;

	public PaymentAuthorizationService(List<PaymentValidatorStrategy> validators, AntiFraudClientPort antiFraudClient,
			PaymentRepositoryPort paymentRepository, CacheManager cacheManager) {
		this.validators = validators;
		this.antiFraudClient = antiFraudClient;
		this.paymentRepository = paymentRepository;
		this.cacheManager = cacheManager;
	}

	@Override
	// Requerimiento #8: Enlazamos el Circuit Breaker con su método de Fallback de
	// contingencia
	@Transactional
	@CircuitBreaker(name = "antifraudService", fallbackMethod = "fallbackAntiFraud")
	public AuthorizationResult authorize(Payment payment) {
		log.info("[INBOUND REQUEST] Recibida solicitud para autorizar la transacción: {}", payment.transactionId());

		// Requerimiento #9: Logs para Validaciones aplicadas (Strategy)
		log.info("[VALIDATION] Aplicando estrategias locales de validación...");
		validators.forEach(validator -> validator.validate(payment));

		// Requerimiento #9: Logs para consulta al proveedor externo
		log.info("[ANTI-FRAUD] Consultando nivel de riesgo al proveedor externo para transacción: {}",
				payment.transactionId());
		String riskLevel = antiFraudClient.checkRiskStatus(payment);
		log.info("[ANTI-FRAUD] El proveedor externo retornó un nivel de riesgo: {}", riskLevel);

		AuthorizationResult result;

		// Requerimientos #6 y #7: Reglas de evaluación del riesgo externo
		if ("LOW_RISK".equalsIgnoreCase(riskLevel)) {
			String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
			result = AuthorizationResultFactory.createApproved(payment.transactionId(), authCode);
			log.info("[AUTHORIZATION RESULT] Transacción {} APROBADA exitosamente.", payment.transactionId());
		} else {
			// 🛠️ CAMBIA ESTA LÍNEA CON EL TEXTO EXACTO QUE DESEAS MOSTRAR:
			result = AuthorizationResultFactory.createRejected(payment.transactionId(),
					"Payment rejected by business rules");
			log.warn("[AUTHORIZATION RESULT] Transacción {} RECHAZADA por detección de riesgo alto.",
					payment.transactionId());
		}

		paymentRepository.save(payment, result);
		return result;
	}

	/**
	 * Requerimiento #8: Fallback controlado en caso de falla o activación del
	 * Circuit Breaker. Debe firmar idéntico al método principal + recibir la
	 * excepción disparada.
	 */
	public AuthorizationResult fallbackAntiFraud(Payment payment, Throwable t) {
		log.error("[CIRCUIT BREAKER / FALLBACK ACTIVATED] El validador externo falló o está caído. Razón: {}",
				t.getMessage());
		log.warn("[FALLBACK] Aplicando regla de contingencia: Rechazo preventivo controlado.");

		// Generamos respuesta controlada usando la fábrica
		AuthorizationResult fallbackResult = AuthorizationResultFactory.createRejected(payment.transactionId(),
				"Payment rejected by business rules (Anti-fraud service unavailable)");

		// Se persiste el rechazo en Postgres para auditoría interna
		paymentRepository.save(payment, fallbackResult);
		return fallbackResult;
	}

	@Override
	public AuthorizationResult findById(String transactionId) {
		log.info("[QUERY] Buscando registro de autorización por ID: {}", transactionId);
		return paymentRepository.findById(transactionId)
				.orElseThrow(() -> new BusinessException("Transaction not found with ID: " + transactionId));
	}
}