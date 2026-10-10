package org.example.crudservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis-backed cache configuration.
 *
 * <p><b>Important:</b> the cache-name constants below are declared as
 * {@code public static final String} literals so they can be referenced
 * inside Spring cache annotations ({@code @Cacheable}, {@code @CacheEvict},
 * ...). Java requires annotation attribute values to be compile-time
 * constants — an array such as {@link #ALL_COUNTRY_CACHES} is <em>not</em>
 * a constant and therefore cannot be used inside an annotation.</p>
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /** Cache name constants — safe to use in @Cacheable / @CacheEvict. */
    public static final String COUNTRY_BY_ISO = "countryByIso";
    public static final String COUNTRY_BY_ID  = "countryById";
    public static final String ALL_COUNTRIES  = "allCountries";

    /**
     * Convenience array for programmatic access (e.g. {@code CacheManager#getCache}).
     * <b>Do not</b> reference this inside a cache annotation — the compiler will
     * reject it with "AttributeValue must be a constant".
     */
    public static final String[] ALL_COUNTRY_CACHES = {
            COUNTRY_BY_ISO, COUNTRY_BY_ID, ALL_COUNTRIES
    };

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Use JSON serialization instead of Java serialization.
        // The polymorphic type validator is restricted to this project's
        // package (plus JDK collections/time) to avoid the well-known
        // Jackson deserialization gadget CVEs.
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("org.example.crudservice.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer))
                .disableCachingNullValues();

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(COUNTRY_BY_ISO, defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put(COUNTRY_BY_ID,  defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put(ALL_COUNTRIES,  defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}