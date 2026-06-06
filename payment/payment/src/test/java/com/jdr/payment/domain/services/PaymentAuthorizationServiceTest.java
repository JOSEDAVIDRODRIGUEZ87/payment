package com.jdr.payment.domain.services;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorizationServiceTest {

	@Mock
	private AntiFraudClientPort antiFraudClient;

	@Mock
	private PaymentRepositoryPort paymentRepository;

	@InjectMocks
	private PaymentAuthorizationService paymentService;

	private Payment validPayment;

	@BeforeEach
	void setUp() {
		// Inicializamos el Record original respetando estrictamente sus 9 campos
		// obligatorios
		validPayment = new Payment("TX-TEST-100", "CUST-001", "MER-900", new BigDecimal("100000.00"), "COP", "CARD",
				"PENDING", LocalDateTime.now(), LocalDateTime.now());

		// Inyectamos una lista vacía via reflexión para evitar el NullPointerException
		// en el forEach de los validadores locales
		ReflectionTestUtils.setField(paymentService, "validators", new ArrayList<>());
	}

	@Test
	@DisplayName("🟢 Debe APROBAR la transacción cuando el proveedor responde LOW_RISK")
	void shouldApprovePaymentWhenLowRisk() {
		// GIVEN
		when(antiFraudClient.checkRiskStatus(any(Payment.class))).thenReturn("LOW_RISK");

		// WHEN
		AuthorizationResult result = paymentService.authorize(validPayment);

		// THEN
		assertNotNull(result);
		assertEquals("APPROVED", result.status());
		assertNotNull(result.authorizationCode());
		assertTrue(result.authorizationCode().startsWith("AUTH-"));

		// Verifica que se persista la auditoría del pago aprobado en la BD
		verify(paymentRepository, times(1)).save(eq(validPayment), any(AuthorizationResult.class));
	}

	@Test
	@DisplayName("🔴 Debe RECHAZAR la transacción cuando el proveedor responde HIGH_RISK")
	void shouldRejectPaymentWhenHighRisk() {
		// GIVEN
		when(antiFraudClient.checkRiskStatus(any(Payment.class))).thenReturn("HIGH_RISK");

		// WHEN
		AuthorizationResult result = paymentService.authorize(validPayment);

		// THEN
		assertNotNull(result);
		assertEquals("REJECTED", result.status());
		assertNull(result.authorizationCode());

		// Verifica que se persista la auditoría del pago rechazado en la BD
		verify(paymentRepository, times(1)).save(eq(validPayment), any(AuthorizationResult.class));
	}

	@Test
	@DisplayName("⚠️ Debe propagar la excepción cuando el proveedor falla para que actúe el Circuit Breaker")
	void shouldApplyFallbackWhenAntiFraudProviderFails() {
		// GIVEN
		String mensajeErrorEsperado = "External Anti-Fraud Provider Timeout";
		when(antiFraudClient.checkRiskStatus(any(Payment.class))).thenThrow(new RuntimeException(mensajeErrorEsperado));

		// WHEN & THEN
		// Con assertThrows capturamos la excepción provocada para que JUnit no rompa el
		// Build
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			paymentService.authorize(validPayment);
		});

		// Validamos que la excepción que sube sea exactamente la que generó el puerto
		// de infraestructura
		assertEquals(mensajeErrorEsperado, exception.getMessage());

		// Como la excepción corta el flujo normal del método de dominio antes de llegar
		// al save,
		// verificamos que el repositorio no haya ejecutado almacenamiento en este
		// punto.
		verify(paymentRepository, times(0)).save(any(), any());
	}
}