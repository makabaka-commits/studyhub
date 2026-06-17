package com.studyhub.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存前缀，避免 key 冲突
    private static final String CACHE_PREFIX = "studyhub:cache:";

    public CacheUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // 支持 LocalDateTime
    }

    /**
     * 存入缓存
     * @param key   缓存 key（会自动加前缀）
     * @param value 要缓存的对象
     * @param ttl   过期时间（秒），null 表示不过期
     */
    public void put(String key, Object value, Long ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            String cacheKey = CACHE_PREFIX + key;
            if (ttl != null) {
                stringRedisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.opsForValue().set(cacheKey, json);
            }
        } catch (JsonProcessingException e) {
            // 序列化失败，忽略缓存
        }
    }

    /**
     * 从缓存获取
     * @param key   缓存 key
     * @param clazz 目标类型
     * @return 反序列化后的对象，缓存不存在返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = stringRedisTemplate.opsForValue().get(CACHE_PREFIX + key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public void evict(String key) {
        stringRedisTemplate.delete(CACHE_PREFIX + key);
    }

    /**
     * 带泛型的获取（用于 List<T> 等复杂类型）
     */
    public <T> T get(String key, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
        try {
            String json = stringRedisTemplate.opsForValue().get(CACHE_PREFIX + key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}