package com.medical.common.utils;

import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    /** 密钥（实际项目中应配置在application.yml中） */
    @Value("${jwt.secret:medical-system-secret-key-2024}")
    private String secret;

    /** 过期时间（小时） */
    @Value("${jwt.expiration:24}")
    private int expiration;

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleCode 角色编码（如 ROLE_admin）
     * @return Token字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", roleCode);
        return createToken(claims);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = DateUtil.offsetHour(now, expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从Token中解析Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 从Token中获取角色编码
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        return !claims.getExpiration().before(new Date());
    }

    /**
     * 检查Token是否即将过期（剩余时间不足1小时）
     */
    public boolean isTokenSoonExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        Date expirationDate = claims.getExpiration();
        // 剩余时间小于1小时则认为即将过期
        return expirationDate.getTime() - System.currentTimeMillis() < 3600000;
    }

    /**
     * 刷新Token（保留原有用户信息和角色）
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", claims.get("userId", Long.class));
        map.put("username", claims.get("username", String.class));
        map.put("role", claims.get("role", String.class));
        return createToken(map);
    }
}
