package com.jdr.payment.ports.inbound;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;

/**
 * Puerto de entrada que define el caso de uso para la autorización de un pago.
 * Expresa una intención pura del negocio independiente del mecanismo de entrega (REST, GRPC, Kafka).
 */
public interface AuthorizePaymentUseCase {

    /**
     * Procesa la solicitud de pago, aplicando reglas de negocio locales 
     * y validaciones externas antifraude.
     *
     * @param payment Objeto de dominio con los datos de la transacción.
     * @return El resultado de la autorización (APPROVED o REJECTED).
     */
    AuthorizationResult authorize(Payment payment);
}