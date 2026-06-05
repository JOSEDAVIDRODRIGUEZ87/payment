-- Creación de la tabla principal de Pagos
CREATE TABLE payments (
    id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(50) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

-- Creación de la tabla de Autorizaciones (Respuestas)
CREATE TABLE payment_authorizations (
    id BIGSERIAL,
    transaction_id VARCHAR(50) NOT NULL,
    authorization_code VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    message VARCHAR(255) NOT NULL,
    risk_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payment_authorizations PRIMARY KEY (id),
    CONSTRAINT fk_authorizations_payments FOREIGN KEY (transaction_id) 
        REFERENCES payments(id) ON DELETE CASCADE
);

-- Índice para acelerar las búsquedas por ID de transacción (Esencial para el GET /api/payments/{transactionId})
CREATE INDEX idx_auth_transaction_id ON payment_authorizations(transaction_id);