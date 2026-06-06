package com.jdr.payment.infrastructure.adapters.inbound.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.infrastructure.adapters.inbound.rest.controllers.PaymentController;
import com.jdr.payment.ports.inbound.AuthorizePaymentUseCase;
import com.jdr.payment.ports.inbound.GetPaymentUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthorizePaymentUseCase authorizePaymentUseCase;

	@MockBean
	private GetPaymentUseCase getPaymentUseCase;

	@Test
	@DisplayName("🟢 POST /api/payments/authorize - Debe retornar 200 OK con la transacción autorizada")
	void shouldReturnOkWhenPaymentRequestIsValid() throws Exception {
		// GIVEN
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("transactionId", "TX-WEB-001");
		requestBody.put("customerId", "CUST-123");
		requestBody.put("merchantId", "MER-456");
		requestBody.put("amount", 50000.00);
		requestBody.put("currency", "COP");
		requestBody.put("paymentMethod", "CARD");

		AuthorizationResult mockResult = new AuthorizationResult("TX-WEB-001", "APPROVED", "AUTH-999", "Success");

		// Cuando se llame con cualquier Payment válido, devolvemos éxito
		when(authorizePaymentUseCase.authorize(any(Payment.class))).thenReturn(mockResult);

		// WHEN & THEN
		mockMvc.perform(post("/api/payments/authorize").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.authorizationCode").value("AUTH-999"));
	}

	@Test
	@DisplayName("🔴 POST /api/payments/authorize - Debe retornar 200 OK con estado REJECTED si faltan campos requeridos")
	void shouldReturnBadRequestWhenFieldsAreMissing() throws Exception {
		// GIVEN: Enviamos un JSON incompleto para obligar a que falle la validación de
		// Jakarta
		Map<String, Object> invalidRequestBody = new HashMap<>();
		invalidRequestBody.put("customerId", "CUST-123"); // Faltan los demás campos requeridos

		// WHEN & THEN: Validamos la estructura real de tu RestControllerAdvice
		mockMvc.perform(post("/api/payments/authorize").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequestBody))).andExpect(status().isOk()) // 🟢 Tu
																											// manejador
																											// intercepta
																											// y
																											// responde
																											// HTTP 200
				.andExpect(jsonPath("$.status").value("REJECTED")) // 🟢 Verifica que el negocio lo rechace
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("required")));
	}

	@Test
	@DisplayName("🔵 GET /api/payments/{transactionId} - Debe retornar la información de la transacción")
	void shouldReturnPaymentWhenTransactionIdExists() throws Exception {
		// GIVEN
		String txId = "TX-WEB-001";
		AuthorizationResult mockResult = new AuthorizationResult(txId, "APPROVED", "AUTH-999", "Fetched Successfully");
		when(getPaymentUseCase.findById(txId)).thenReturn(mockResult);

		// WHEN & THEN
		mockMvc.perform(get("/api/payments/{transactionId}", txId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.transactionId").value(txId))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}
}