package com.jdr.payment.infrastructure.adapters.outbound.feign;

import com.jdr.payment.domain.models.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AntiFraudAdapterTest {

	@Mock
	private AntiFraudFeignClient feignClient;

	@InjectMocks
	private AntiFraudAdapter antiFraudAdapter; // 🟢 Nombre exacto de tu componente real

	private Payment payment;

	@BeforeEach
	void setUp() {
		payment = new Payment("TX-INFRA-001", "CUST-1", "MER-1", new BigDecimal("75000"), "COP", "CARD", "PENDING",
				LocalDateTime.now(), LocalDateTime.now());
	}

	@Test
	@DisplayName("🔍 Debe mapear la petición y retornar el riskLevel desde la respuesta del cliente Feign")
	void shouldReturnRiskStatusFromFeignClient() {
		// GIVEN: En lugar de usar 'new', creamos un mock de la respuesta
		AntiFraudFeignClient.FraudCheckResponse mockResponse = mock(AntiFraudFeignClient.FraudCheckResponse.class);

		// Configuramos el comportamiento del mock para que devuelva "LOW_RISK" al
		// llamar a su método
		// 💡 NOTA: Si tu método no se llama riskLevel(), cámbialo por el nombre real
		// (ej: getRiskLevel() o status())
		when(mockResponse.riskLevel()).thenReturn("LOW_RISK");

		// Simulamos el comportamiento del cliente Feign cuando se le pasa cualquier
		// Request
		when(feignClient.checkTransaction(any(AntiFraudFeignClient.FraudCheckRequest.class))).thenReturn(mockResponse);

		// WHEN: Ejecutamos el método del adaptador
		String riskStatus = antiFraudAdapter.checkRiskStatus(payment);

		// THEN: Comprobamos el mapeo exitoso
		assertEquals("LOW_RISK", riskStatus);

		// Verificación de seguridad
		verify(feignClient, times(1)).checkTransaction(any(AntiFraudFeignClient.FraudCheckRequest.class));
	}
}