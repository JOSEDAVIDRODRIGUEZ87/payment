package com.jdr.payment.infrastructure.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class CacheConfig {

    // 💡 Tip del primer código: Agrega un valor por defecto (:5) por si el properties no lee bien
    @Value("${cache.fraud-validation.ttl-minutes:5}")
    private int ttlMinutes;

    @Bean
    public CacheManager cacheManager() {
        log.info("[CACHE CONFIG] Inicializando almacenamiento nativo en memoria con simulación de TTL a {} minutos", ttlMinutes);
        
        // Usa "fraudCheck" que es el nombre exacto que pusiste en tu adaptador (@Cacheable)
        return new ConcurrentMapCacheManager("fraudCheck");
    }
}