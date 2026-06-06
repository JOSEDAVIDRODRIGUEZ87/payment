package com.jdr.payment.infrastructure.adapters.outbound.feign;

import com.jdr.payment.domain.models.Payment;
import com.jdr.payment.ports.outbound.AntiFraudClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import org.springframework.cache.annotation.Cacheable;

@Component
@RequiredArgsConstructor
@Slf4j
public class AntiFraudAdapter implements AntiFraudClientPort {

	private final AntiFraudFeignClient feignClient;

	// Asegúrate de importar la de Spring


	@Override
	// 🛠️ 'fraudCheck' coincide con el nombre del Bean. 
	// Usamos el customerId o el transactionId según la estrategia. 
	// El requerimiento dice "de una misma transacción", por ende la clave ideal es el transactionId.
	@Cacheable(value = "fraudCheck", key = "#payment.transactionId")
	public String checkRiskStatus(Payment payment) {
	    
	    // 💡 NOTA SENIOR: Si el flujo entra a ejecutar este método, significa que el dato NO estaba en la caché.
	    // Si el dato ya hubiese estado en caché, Spring intercepta la llamada, no ejecuta este código y devuelve el valor directo.
	    
	    log.info("[ANTI-FRAUD VIA HTTP] Cache MISS - Consultando proveedor externo antifraude para Tx: {}", payment.transactionId());
	    
	    AntiFraudFeignClient.FraudCheckRequest requestBody = new AntiFraudFeignClient.FraudCheckRequest(
	            payment.transactionId(),
	            payment.customerId(),
	            payment.amount()
	    );
	    
	    AntiFraudFeignClient.FraudCheckResponse response = feignClient.checkTransaction(requestBody);
	    
	    log.info("[HTTP Response] Proveedor respondió riesgo: {} para Tx: {}", response.riskLevel(), payment.transactionId());
	    return response.riskLevel();
	}
}