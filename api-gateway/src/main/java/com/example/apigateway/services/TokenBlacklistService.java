package com.example.apigateway.services;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenBlacklistService {

    // Must match the Auth Service's TokenBlacklistService.PREFIX exactly.
    private static final String PREFIX = "token:blacklist:";

    private final ReactiveStringRedisTemplate redis;

    public TokenBlacklistService(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    public Mono<Boolean> isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) return Mono.just(false);
        return redis.hasKey(PREFIX + jti)
                .defaultIfEmpty(false)
                .onErrorReturn(true);   // Redis unreachable => fail closed
    }
}