package com.jdr.payment.infrastructure.adapters.outbound.persistence;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.PaymentEntity;
import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.AuthorizationResultEntity; // 🔍 Importa tu otra entidad
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final JpaPaymentRepository jpaRepository; 
    private final JpaAuthorizationResultRepository authResultRepository; // 🛠️ 1. Inyecta el repositorio de autorizaciones

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public void save(Payment payment, AuthorizationResult result) {
        log.info("[SQL PERSISTENCE] Insertando registro completo en public.payments para Tx: {}", payment.transactionId());
        
        try {
            // 2. Guardamos en la tabla public.payments
            PaymentEntity paymentEntity = PaymentEntity.builder()
                    .id(payment.transactionId())
                    .customerId(payment.customerId())
                    .merchantId(payment.merchantId())
                    .amount(payment.amount())
                    .currency(payment.currency())
                    .paymentMethod(payment.paymentMethod())
                    .status(result.status()) 
                    .build();

            jpaRepository.save(paymentEntity);
            log.info("[SQL PERSISTENCE] ¡Registro de Pago guardado con éxito!");

            // 🛠️ 3. MAPEO E INSERT EN LA TABLA public.authorization_results
            log.info("[SQL PERSISTENCE] Insertando auditoría en public.authorization_results para Tx: {}", payment.transactionId());
            
            AuthorizationResultEntity authEntity = AuthorizationResultEntity.builder()
                    .transactionId(result.transactionId())         // varchar(50) -> PK o FK
                    .authorizationCode(result.authorizationCode()) // varchar(50) (puede ser null)
                    .message(result.message())                     // varchar(255)
                    .status(result.status())                       // varchar(20)
                    .build();

            authResultRepository.save(authEntity); // 👈 ¡Aquí llenamos la tabla vacía!
            log.info("[SQL PERSISTENCE] ¡Registro de Autorización guardado con éxito!");

        } catch (Exception e) {
            log.error("[SQL PERSISTENCE ERROR] Error al insertar en Postgres: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorizationResult> findById(String transactionId) {
        log.info("[SQL PERSISTENCE] Buscando autorización por ID: {}", transactionId);
        
        // 🛠️ 4. Tip Senior: Si la tabla de verdad existe, tu consulta de abajo debería 
        // buscar en 'authResultRepository' en lugar de calcular el código hash en caliente.
        // Pero si el requerimiento de la prueba pide leer de la tabla 'payments', tu código actual está perfecto.
        return jpaRepository.findById(transactionId)
                .map(entity -> AuthorizationResult.builder()
                        .transactionId(entity.getId())
                        .status(entity.getStatus())
                        .authorizationCode(entity.getStatus().equals("APPROVED") ? "AUTH-" + Math.abs(entity.getId().hashCode()) : null)
                        .message("Payment found in database with status: " + entity.getStatus())
                        .build());
    }
}