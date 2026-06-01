package com.medical.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    // ==================== String操作 ====================

    /**
     * 设置缓存
     */
    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     */
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public <T> T get(String key) {
        ValueOperations<String, T> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire != null ? expire : -1;
    }

    /**
     * 判断Key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ==================== 删除操作 ====================

    /**
     * 删除单个Key
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除Key
     */
    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null ? count : 0;
    }

    /**
     * 按前缀删除Key
     */
    public long deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            return delete(keys);
        }
        return 0;
    }

    // ==================== Hash操作 ====================

    public <T> void hSet(String key, String field, T value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    public Map<String, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void hDelete(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    // ==================== List操作 ====================

    public <T> long lPush(String key, T value) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return result != null ? result : 0;
    }

    public <T> List<T> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ==================== Set操作 ====================

    public <T> long sAdd(String key, T... values) {
        Long result = redisTemplate.opsForSet().add(key, values);
        return result != null ? result : 0;
    }

    public <T> Set<T> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ==================== ZSet操作 ====================

    public <T> boolean zAdd(String key, T value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
    }

    public <T> Set<T> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    // ==================== 计数器 ====================

    /**
     * 自增
     */
    public long incr(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /**
     * 自减
     */
    public long decr(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0;
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     *
     * @param key     锁的Key
     * @param value   锁的值（用于解锁时校验）
     * @param timeout 超时时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String value, long timeout) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS)
        );
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String key, String value) {
        String currentValue = get(key);
        if (value.equals(currentValue)) {
            return delete(key);
        }
        return false;
    }
}
