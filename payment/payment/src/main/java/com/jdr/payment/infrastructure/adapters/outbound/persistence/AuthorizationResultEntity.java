package com.jdr.payment.infrastructure.adapters.outbound.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "authorization_results")
public class AuthorizationResultEntity {
    
    @Id
    private String transactionId;
    private String status;
    private String authorizationCode;
    private String message;

    // Constructor vacío exigido por JPA
    public AuthorizationResultEntity() {
    }

    // Constructor completo para facilitar el mapeo
    public AuthorizationResultEntity(String transactionId, String status, String authorizationCode, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.authorizationCode = authorizationCode;
        this.message = message;
    }

    // Getters y Setters tradicionales (evitamos records aquí)
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}