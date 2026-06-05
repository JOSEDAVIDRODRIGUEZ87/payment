package com.jdr.payment.infrastructure.adapters.outbound.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching // Habilita la infraestructura de caché de Spring
@Slf4j
public class CacheConfig {

    // Inyecta el TTL desde el archivo application.yml, por defecto 5 minutos si no existe
    @Value("${cache.fraud-validation.ttl-minutes:5}")
    private long ttlMinutes;

    @Bean
    public CacheManager cacheManager() {
        log.info("[Cache Config] Inicializando Caffeine Cache Manager con un TTL de {} minutos", ttlMinutes);
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("fraudRiskCache");
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // Define el tiempo de expiración desde la última escritura
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                // Controla el tamaño máximo en memoria para evitar desbordamientos (OutOfMemory)
                .maximumSize(10000)
                // Habilita estadísticas si deseas monitorear el rendimiento en producción
                .recordStats());
                
        return cacheManager;
    }
}