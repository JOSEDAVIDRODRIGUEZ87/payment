package com.jdr.payment.infrastructure.adapters.outbound.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authorization_results", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationResultEntity {

    @Id
    @Column(name = "transaction_id", length = 50, nullable = false)
    private String transactionId; // PK que conecta directamente con el ID del pago

    @Column(name = "authorization_code", length = 50)
    private String authorizationCode; // Puede ser nulo si el estado es REJECTED

    @Column(name = "message", length = 255, nullable = false)
    private String message; // Razón del rechazo o confirmación de aprobación

    @Column(name = "status", length = 20, nullable = false)
    private String status; // APPROVED o REJECTED
}