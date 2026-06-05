package com.jdr.payment.domain.services;

import java.util.List;
import java.util.UUID;
import org.springframework.cache.CacheManager; // <- Asegúrate de tener este import

import com.jdr.payment.domain.exceptions.BusinessException;
import com.jdr.payment.domain.factory.AuthorizationResultFactory;
import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.ports.inbound.AuthorizePaymentUseCase;
import com.jdr.payment.ports.inbound.GetPaymentUseCase;
import com.jdr.payment.ports.inbound.PaymentValidatorStrategy;
import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;

public class PaymentAuthorizationService implements AuthorizePaymentUseCase, GetPaymentUseCase {

    private final List<PaymentValidatorStrategy> validators;
    private final AntiFraudClientPort antiFraudClient;
    private final PaymentRepositoryPort paymentRepository;
    private final CacheManager cacheManager; // 1. Agrega el atributo de la caché

    // 2. Actualiza el constructor para recibir los 4 parámetros
    public PaymentAuthorizationService(List<PaymentValidatorStrategy> validators, 
                                       AntiFraudClientPort antiFraudClient, 
                                       PaymentRepositoryPort paymentRepository,
                                       CacheManager cacheManager) {
        this.validators = validators;
        this.antiFraudClient = antiFraudClient;
        this.paymentRepository = paymentRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public AuthorizationResult authorize(Payment payment) {
        // ... Tu lógica actual del método authorize
        validators.forEach(validator -> validator.validate(payment));
        
        String riskLevel = antiFraudClient.checkRiskStatus(payment.transactionId());
        
        AuthorizationResult result;
        if ("LOW_RISK".equalsIgnoreCase(riskLevel)) {
            String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            result = AuthorizationResultFactory.createApproved(payment.transactionId(), authCode);
        } else {
            result = AuthorizationResultFactory.createRejected(payment.transactionId(), "Payment rejected by anti-fraud policy");
        }

        paymentRepository.save(payment, result);
        return result;
    }

    @Override
    public AuthorizationResult findById(String transactionId) {
        return paymentRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found with ID: " + transactionId));
    }
}