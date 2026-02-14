package com.tech.aidocqna.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializationContext.SerializationPair<Object> defaultValueSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        RedisSerializer.json()
                );

        RedisSerializationContext.SerializationPair<String> keySerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()
                );

        RedisSerializationContext.SerializationPair<List> embeddingValueSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(List.class)
                );

        RedisCacheConfiguration baseConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeKeysWith(keySerializer)
                        .serializeValuesWith(defaultValueSerializer);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withCacheConfiguration("summary",
                        baseConfig
                                .entryTtl(Duration.ofHours(6)))
                .withCacheConfiguration("faq",
                        baseConfig
                                .entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration("embeddings",
                        baseConfig
                                .serializeValuesWith(embeddingValueSerializer)
                                .entryTtl(Duration.ofHours(12)))
                .build();
    }
}
