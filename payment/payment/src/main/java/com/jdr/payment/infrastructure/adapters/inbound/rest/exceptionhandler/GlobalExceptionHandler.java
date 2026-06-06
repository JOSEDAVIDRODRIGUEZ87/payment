package com.jdr.payment.infrastructure.adapters.inbound.rest.exceptionhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 🛠️ Cambiamos el tipo de retorno de Map a PaymentResponse
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<PaymentResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

		// 1. Intentamos rescatar el transactionId real desde el DTO que falló
		String realTransactionId = "UNKNOWN_TX";

		if (ex.getBindingResult()
				.getTarget() instanceof com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentRequest request) {
			if (request.transactionId() != null && !request.transactionId().isBlank()) {
				realTransactionId = request.transactionId(); // 🔍 ¡AQUÍ RESCATAMOS TU ID REAL!
			}
		}

		// 2. Extraemos el mensaje de error de validación
		String defaultMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Validation failed");

		// 3. Construimos la respuesta con el ID real recuperado
		PaymentResponse errorResponse = new PaymentResponse(realTransactionId, // Ya no estará en UNKNOWN_TX
				"REJECTED", null, "Payment rejected by business rules: " + defaultMessage);

		return ResponseEntity.badRequest().body(errorResponse);
	}
}