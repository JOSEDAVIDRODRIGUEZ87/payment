package com.jdr.payment.infrastructure.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResilienceConfig {

    // Inyectamos el registro central de Resilience4j que lee el application.properties
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void monitorCircuitBreakers() {
        log.info("[Resilience Config] Inicializando monitoreo de eventos para Circuit Breakers");

        // Buscamos la instancia específica que configuramos para el Feign del Antifraude
        CircuitBreaker antiFraudCircuit = circuitBreakerRegistry.circuitBreaker("antiFraudService");

        // Escuchamos cuando el circuito cambia de estado (ej: CERRADO -> ABIERTO)
        antiFraudCircuit.getEventPublisher().onStateTransition(event -> {
            log.warn("[CIRCUIT BREAKER STATE CHANGE] El circuito '{}' cambio de estado: {} -> {}",
                    event.getCircuitBreakerName(),
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState());
        });

        // Escuchamos cuando se registra una llamada errónea (Timeout o Error HTTP 5xx)
        antiFraudCircuit.getEventPublisher().onError(event -> {
            log.error("[CIRCUIT BREAKER ERROR RECORDED] Fallo detectado en el servicio externo. Duracion: {}ms. Motivo: {}",
                    event.getElapsedDuration().toMillis(),
                    event.getThrowable().getMessage());
        });

        // Escuchamos cuando el circuito rechaza llamadas directamente por estar en estado ABIERTO
        antiFraudCircuit.getEventPublisher().onCallNotPermitted(event -> {
            log.warn("[CIRCUIT BREAKER BLOCKED] Llamada rechazada de inmediato. El circuito esta ABIERTO. Protegiendo el sistema.");
        });
    }
}