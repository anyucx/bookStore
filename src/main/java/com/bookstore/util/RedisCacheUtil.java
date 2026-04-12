package com.bookstore.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void setCacheObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setCacheObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCacheObject(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public boolean deleteObject(String key) {
        Boolean ok = redisTemplate.delete(key);
        return ok != null && ok;
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean ok = redisTemplate.expire(key, timeout, unit);
        return ok != null && ok;
    }
}
