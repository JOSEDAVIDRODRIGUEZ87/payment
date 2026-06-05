package com.jdr.payment.infrastructure.adapters.outbound.persistence.mappers;

import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.infrastructure.adapters.outbound.persistence.entities.PaymentEntity;

public class PaymentPersistenceMapper {

    public static Payment toDomain(PaymentEntity entity) {
        if (entity == null) return null;
        return new Payment(
                entity.getId(),
                entity.getCustomerId(),
                entity.getMerchantId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getPaymentMethod(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PaymentEntity toEntity(Payment domain) {
        if (domain == null) return null;
        return PaymentEntity.builder()
                .id(domain.transactionId())
                .customerId(domain.customerId())
                .merchantId(domain.merchantId())
                .amount(domain.amount())
                .currency(domain.currency())
                .paymentMethod(domain.paymentMethod())
                .status(domain.status())
                .createdAt(domain.createdAt() != null ? domain.createdAt() : java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}