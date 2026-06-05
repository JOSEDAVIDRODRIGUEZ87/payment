package com.jdr.payment.domain.factory;

import com.jdr.payment.domain.models.AuthorizationResult;

/**
 * Fábrica basada en el patrón Factory para estandarizar 
 * la creación de respuestas del dominio.
 */
public class AuthorizationResultFactory {

    private AuthorizationResultFactory() {
        // Constructor privado para evitar instanciación de clases utilitarias
    }

    public static AuthorizationResult createApproved(String transactionId, String authCode) {
        return AuthorizationResult.builder()
                .transactionId(transactionId)
                .status("APPROVED")
                .authorizationCode(authCode)
                .message("Payment authorized")
                .build();
    }

    public static AuthorizationResult createRejected(String transactionId, String reason) {
        return AuthorizationResult.builder()
                .transactionId(transactionId)
                .status("REJECTED")
                .message(reason)
                .build();
    }
}