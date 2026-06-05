package com.jdr.payment.infrastructure.adapters.outbound.persistence;

import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.PaymentEntity; // Asegúrate de importar tu entidad correcta
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
    // Ahora Spring Data sabe que este repositorio maneja la tabla public.payments 
    // y acepta objetos de tipo PaymentEntity en su método .save()
}