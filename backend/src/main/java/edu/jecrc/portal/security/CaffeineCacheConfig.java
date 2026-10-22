package edu.jecrc.portal.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class CaffeineCacheConfig {

    @Data
    @AllArgsConstructor
    public static class UserSecuritySnapshot {
        private UUID securityStamp;
        private boolean active;
        private boolean suspended;
    }

    @Bean
    public Cache<UUID, UserSecuritySnapshot> userSecurityCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(10_000)
                .build();
    }
}
