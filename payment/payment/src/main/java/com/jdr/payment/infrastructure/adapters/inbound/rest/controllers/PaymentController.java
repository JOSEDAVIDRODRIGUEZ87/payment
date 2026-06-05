package com.jdr.payment.infrastructure.adapters.inbound.rest.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jdr.payment.domain.models.AuthorizationResult;
import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentRequest;
import com.jdr.payment.infrastructure.adapters.inbound.rest.dtos.PaymentResponse;
import com.jdr.payment.infrastructure.adapters.inbound.rest.mappers.PaymentRestMapper;
import com.jdr.payment.ports.inbound.AuthorizePaymentUseCase;
import com.jdr.payment.ports.inbound.GetPaymentUseCase;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    // Inyectamos las INTERFACES (Puertos), nunca las implementaciones directas
    private final AuthorizePaymentUseCase authorizePaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorize(@Valid @RequestBody PaymentRequest request) {
        log.info("[HTTP POST] Request received to authorize transaction: {}", request.transactionId());
        
        // 1. Convertir DTO externo a objeto de Dominio interno
        Payment paymentDomain = PaymentRestMapper.toDomain(request);
        
        // 2. Ejecutar lógica de negocio
        AuthorizationResult result = authorizePaymentUseCase.authorize(paymentDomain);
        
        // 3. Convertir resultado de negocio a DTO de respuesta externa
        PaymentResponse response = PaymentRestMapper.toResponse(result);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getByTransactionId(@PathVariable String transactionId) {
        log.info("[HTTP GET] Request received to fetch transaction: {}", transactionId);
        
        // Ejecuta el caso de uso de búsqueda
        AuthorizationResult result = getPaymentUseCase.findById(transactionId);
        
        return ResponseEntity.ok(PaymentRestMapper.toResponse(result));
    }
}