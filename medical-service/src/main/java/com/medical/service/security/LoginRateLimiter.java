package com.medical.service.security;

import com.medical.common.constant.RedisConstants;
import com.medical.common.enums.ResultCode;
import com.medical.common.exception.BusinessException;
import com.medical.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录频率限制器 — 防止暴力破解
 */
@Slf4j
@Component
public class LoginRateLimiter {

    @Autowired
    private RedisUtil redisUtil;

    /** 同一IP每分钟最大尝试次数 */
    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;
    /** 同一用户名每分钟最大尝试次数 */
    private static final int MAX_ATTEMPTS_PER_USER = 10;

    /**
     * 检查登录频率（IP维度 + 用户名维度双重限制）
     */
    public void checkRateLimit(String username, String ip) {
        checkIpLimit(ip);
        checkUserLimit(username);
    }

    /**
     * 记录失败尝试（密码错误时调用）
     */
    public void recordFailedAttempt(String username, String ip) {
        String ipKey = RedisConstants.RATE_LIMIT_KEY + "login:ip:" + ip;
        String userKey = RedisConstants.RATE_LIMIT_KEY + "login:user:" + username;

        long ipCount = redisUtil.incr(ipKey, 1);
        long userCount = redisUtil.incr(userKey, 1);

        // 首次设置过期时间
        if (ipCount == 1) {
            redisUtil.expire(ipKey, 1, TimeUnit.MINUTES);
        }
        if (userCount == 1) {
            redisUtil.expire(userKey, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * 登录成功后清除限制计数
     */
    public void clearRateLimit(String username, String ip) {
        redisUtil.delete(RedisConstants.RATE_LIMIT_KEY + "login:ip:" + ip);
        redisUtil.delete(RedisConstants.RATE_LIMIT_KEY + "login:user:" + username);
    }

    /** IP维度限制 */
    private void checkIpLimit(String ip) {
        String key = RedisConstants.RATE_LIMIT_KEY + "login:ip:" + ip;
        Integer count = redisUtil.get(key);
        if (count != null && count >= MAX_ATTEMPTS_PER_MINUTE) {
            log.warn("IP登录频率超限: ip={}, count={}", ip, count);
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "操作过于频繁，请稍后再试");
        }
    }

    /** 用户名维度限制 */
    private void checkUserLimit(String username) {
        String key = RedisConstants.RATE_LIMIT_KEY + "login:user:" + username;
        Integer count = redisUtil.get(key);
        if (count != null && count >= MAX_ATTEMPTS_PER_USER) {
            log.warn("用户登录频率超限: username={}, count={}", username, count);
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "操作过于频繁，请稍后再试");
        }
    }
}
