package com.jdr.payment.ports.inbound;

import com.jdr.payment.domain.models.AuthorizationResult;

/**
 * Puerto de entrada para el caso de uso de consulta de pagos.
 */
public interface GetPaymentUseCase {

    /**
     * Busca el resultado de una autorización mediante su ID único de transacción.
     * * @param transactionId ID de la transacción a consultar.
     * @return El resultado de la autorización (APPROVED/REJECTED) encontrado.
     */
    AuthorizationResult findById(String transactionId);
}