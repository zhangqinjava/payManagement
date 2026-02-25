package com.ai.config;

import com.ai.bean.vo.MerchantVo;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class MerchantCache {

    private static final String MERCHANT_KEY = "m:b:";
    private static final String NULL_KEY = "m:null:";

    private static final long TTL_MINUTES = 30;

    @Resource
    private StringRedisTemplate redisTemplate;

    public String buildKey(String merchantId) {
        return MERCHANT_KEY + merchantId;
    }

    public MerchantVo get(String merchantId) {
        String key = buildKey(merchantId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        // 续期
        redisTemplate.expire(key, ttlWithJitter(), TimeUnit.MINUTES);
        return JSON.parseObject(json, MerchantVo.class);
    }

    public void set(MerchantVo merchant) {
        String key = buildKey(merchant.getMerchantId());
        redisTemplate.opsForValue().set(
                key,
                JSON.toJSONString(merchant),
                ttlWithJitter(),
                TimeUnit.MINUTES
        );
    }

    public void delete(String merchantId) {
        redisTemplate.delete(buildKey(merchantId));
    }

    public boolean isNullCached(String merchantId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(NULL_KEY + merchantId)
        );
    }

    public void cacheNull(String merchantId) {
        redisTemplate.opsForValue()
                .set(NULL_KEY + merchantId, "1", 1, TimeUnit.MINUTES);
    }

    private long ttlWithJitter() {
        return TTL_MINUTES + ThreadLocalRandom.current().nextInt(5);
    }
}
