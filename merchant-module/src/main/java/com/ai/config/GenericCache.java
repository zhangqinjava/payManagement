package com.ai.config;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class GenericCache {

    private static final String NULL_KEY_PREFIX = "null:";
    private static final long DEFAULT_TTL_MINUTES = 30;
    private static final int JITTER_RANGE_MINUTES = 5;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /* ===================== 对外核心 API ===================== */

    public <T> T get(String key, Class<T> clazz) {
        // 1. NULL 缓存命中
        if (isNullCached(key)) {
            return null;
        }
        // 2. 正常缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        // 3. 续期
        renew(key);
        return JSON.parseObject(json, clazz);
    }
    public <T> T get(String key, Class<T> clazz, Long ttl, TimeUnit timeUnit) {
        // 1. NULL 缓存命中
        if (isNullCached(key)) {
            return null;
        }
        // 2. 正常缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        // 3. 续期
        renew(key, ttl, timeUnit);
        return JSON.parseObject(json, clazz);
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL_MINUTES);
    }

    public void set(String key, Object value, long ttlMinutes) {
        Objects.requireNonNull(value, "cache value must not be null");
        stringRedisTemplate.opsForValue().set(
                key,
                JSON.toJSONString(value),
                ttlWithJitter(ttlMinutes),
                TimeUnit.MINUTES
        );
    }
    public void set(String key, Object value, long ttlMinutes, TimeUnit timeUnit) {
        Objects.requireNonNull(value, "cache value must not be null");
        stringRedisTemplate.opsForValue().set(
                key,
                JSON.toJSONString(value),
                ttlWithJitter(ttlMinutes),
                timeUnit
        );
    }

    public void delete(String key) {
        if (exists(key)) {
            stringRedisTemplate.delete(key);
        }
        if (isNullCached(key)) {
            stringRedisTemplate.delete(nullKey(key));
        }
    }
    /* ===================== NULL 防穿透 ===================== */
    public void cacheNull(String key) {
        stringRedisTemplate.opsForValue()
                .set(nullKey(key), "1", 1, TimeUnit.MINUTES);
    }
    public void cacheNull(String prefix,String key) {
        stringRedisTemplate.opsForValue()
                .set(nullKey(prefix,key), null, 1, TimeUnit.MINUTES);
    }
    public boolean isNullCached(String key) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(nullKey(key))
        );
    }
    /* ===================== 内部方法 ===================== */
    private void renew(String key) {
        stringRedisTemplate.expire(
                key,
                ttlWithJitter(DEFAULT_TTL_MINUTES),
                TimeUnit.MINUTES
        );
    }
    private void renew(String key, long ttlMinutes, TimeUnit timeUnit) {
        stringRedisTemplate.expire(
                key,
                ttlWithJitter(ttlMinutes),
                timeUnit
        );
    }

    private long ttlWithJitter(long ttlMinutes) {
        return ttlMinutes + ThreadLocalRandom.current().nextInt(JITTER_RANGE_MINUTES);
    }
    private String nullKey(String key) {
        return NULL_KEY_PREFIX + key;
    }
    private String nullKey(String prefix,String key) {
        return prefix + key;
    }
    public boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
