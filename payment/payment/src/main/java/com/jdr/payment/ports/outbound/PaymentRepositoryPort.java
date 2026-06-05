package com.jdr.payment.ports.outbound;

import java.util.Optional;
import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment; // Asegúrate de importar tu modelo de pago

public interface PaymentRepositoryPort {
    // Cambiamos el save para que reciba ambos y llene toda la tabla payments
    void save(Payment payment, AuthorizationResult result);
    
    // Busca por ID y devuelve el resultado de la autorización
    Optional<AuthorizationResult> findById(String transactionId); 
}