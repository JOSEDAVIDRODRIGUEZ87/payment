package com.jdr.payment.domain.models;

public record AuthorizationResult(
    String transactionId,
    String status,
    String authorizationCode,
    String message
) {
    // Implementación manual del patrón Builder para el Record
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String transactionId;
        private String status;
        private String authorizationCode;
        private String message;

        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder authorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public AuthorizationResult build() {
            return new AuthorizationResult(transactionId, status, authorizationCode, message);
        }
    }
}