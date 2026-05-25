package com.fitness.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fitness.common.cache.LoggingCacheManager;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.common.cache.RedisCacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        //jack son serializer -- values 序列化格式：json
        GenericJackson2JsonRedisSerializer valueSerializer = buildValueSerializer();
        // key 序列化：  string
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // redis ： key（string）：value(json)

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = buildValueSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("fitness:cache:");

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(RedisCacheNames.ACTIVE_BANNERS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(RedisCacheNames.DICT_OPTIONS, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put(RedisCacheNames.COURSE_PUBLIC_LIST, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(RedisCacheNames.COURSE_CATEGORIES, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(RedisCacheNames.COURSE_HOME_CATEGORIES, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(RedisCacheNames.COURSE_HOME_CARDS, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(RedisCacheNames.UPCOMING_SESSIONS, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(RedisCacheNames.COACH_HOME, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(RedisCacheNames.PRODUCT_PUBLIC_LIST, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(RedisCacheNames.USER_PERMISSIONS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(RedisCacheNames.USER_ROLES, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
        return new LoggingCacheManager(cacheManager);
    }

    private GenericJackson2JsonRedisSerializer buildValueSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
