package com.jdr.payment.infrastructure.adapters.outbound.persistence;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment; // Asegúrate de que esta ruta a tu modelo Payment sea la correcta
import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.PaymentEntity;
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

    private final JpaPaymentRepository jpaRepository; // Tu interfaz JpaRepository

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Protege el INSERT ante fallos de red del proveedor externo
    public void save(Payment payment, AuthorizationResult result) {
        log.info("[SQL PERSISTENCE] Insertando registro completo en public.payments para Tx: {}", payment.transactionId());
        
        try {
            // Mapeo completo utilizando los datos del pago y del resultado
            PaymentEntity entity = PaymentEntity.builder()
                    .id(payment.transactionId()) // varchar(50) -> PK
                    .customerId(payment.customerId()) // varchar(50)
                    .merchantId(payment.merchantId()) // varchar(50)
                    .amount((payment.amount())) // numeric(15,2)
                    .currency(payment.currency()) // varchar(3)
                    .paymentMethod(payment.paymentMethod()) // varchar(20)
                    .status(result.status()) // varchar(20) -> APPROVED / REJECTED
                    .build();

            jpaRepository.save(entity);
            log.info("[SQL PERSISTENCE] ¡Registro guardado con éxito!");
        } catch (Exception e) {
            log.error("[SQL PERSISTENCE ERROR] Error al insertar en Postgres: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorizationResult> findById(String transactionId) {
        log.info("[SQL PERSISTENCE] Buscando autorización por ID: {}", transactionId);
        
        return jpaRepository.findById(transactionId)
                .map(entity -> AuthorizationResult.builder()
                        .transactionId(entity.getId())
                        .status((entity.getStatus()))
                        .authorizationCode(entity.getStatus().equals("APPROVED") ? "AUTH-" + Math.abs(entity.getId().hashCode()) : null)
                        .message("Payment found in database with status: " + entity.getStatus())
                        .build());
    }
}