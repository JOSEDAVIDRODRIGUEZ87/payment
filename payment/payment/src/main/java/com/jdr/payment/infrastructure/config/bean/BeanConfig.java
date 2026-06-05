package com.jdr.payment.infrastructure.config.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;

import com.jdr.payment.domain.services.PaymentAuthorizationService;
import com.jdr.payment.ports.inbound.AuthorizePaymentUseCase;
import com.jdr.payment.ports.inbound.GetPaymentUseCase; // <-- Asegúrate de agregar este import
import com.jdr.payment.ports.inbound.PaymentValidatorStrategy;
import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import com.jdr.payment.ports.outbound.PaymentRepositoryPort;

import java.util.List;

@Configuration
public class BeanConfig {

    // 1. Registramos el Servicio de Dominio principal con sus 4 dependencias
    @Bean
    public PaymentAuthorizationService paymentAuthorizationService(
            List<PaymentValidatorStrategy> validators,
            AntiFraudClientPort antiFraudClient,
            PaymentRepositoryPort paymentRepository,
            CacheManager cacheManager) {
            
        return new PaymentAuthorizationService(validators, antiFraudClient, paymentRepository, cacheManager);
    }

    // 2. Exponemos el puerto de entrada para el POST (Autorizar) pasándole el servicio anterior
    @Bean
    public AuthorizePaymentUseCase authorizePaymentUseCase(PaymentAuthorizationService service) {
        return service;
    }

    // 3. Exponemos el puerto de entrada para el GET (Consultar) pasándole el mismo servicio
    @Bean
    public GetPaymentUseCase getPaymentUseCase(PaymentAuthorizationService service) {
        return service;
    }
}