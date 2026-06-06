package com.jdr.payment.application.usecases;


import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.ports.inbound.AuthorizePaymentInputPort; // Importas tu puerto
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;
import com.jdr.payment.infrastructure.adapters.outbound.http.AntifraudHttpAdapter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class AuthorizePaymentUseCase implements AuthorizePaymentInputPort { // <-- Implementas el puerto

    private final AntifraudHttpAdapter antifraudHttpAdapter;
    private final PaymentRepositoryPort paymentRepository;

    @Override // Cumples con el contrato del puerto de entrada
    @CircuitBreaker(name = "antifraudService", fallbackMethod = "fallbackAntiFraud")
    public AuthorizationResult execute(Payment payment) {
        log.info("[USE CASE] Iniciando flujo de autorización para transacción: {}", payment.transactionId());
        
        // ... (Todo el resto del código que armamos en el paso anterior se queda exactamente igual) ...
        
        return AuthorizationResult.builder().build(); // Tu lógica real armada
    }

    public AuthorizationResult fallbackAntiFraud(Payment payment, Throwable t) {
        log.error("[FALLBACK ACTIVATED] Servicio antifraude caído. Razón: {}", t.getMessage());
        return AuthorizationResult.builder()
                .transactionId(payment.transactionId())
                .status("REJECTED")
                .message("Payment rejected by business rules (Anti-fraud service unavailable)")
                .build();
    }
}