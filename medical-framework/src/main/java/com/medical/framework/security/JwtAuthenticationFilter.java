package com.medical.framework.security;

import cn.hutool.core.util.StrUtil;
import com.medical.common.constant.Constants;
import com.medical.common.utils.JwtUtil;
import com.medical.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);

        if (StrUtil.isNotBlank(token)) {
            try {
                // 验证Token
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);

                    // 检查Redis中是否存在用户登录信息（支持Token主动失效）
                    String redisKey = Constants.LOGIN_USER_KEY + userId;
                    String cachedToken = redisUtil.get(redisKey);

                    if (StrUtil.isNotBlank(cachedToken) && token.equals(cachedToken)) {
                        // 设置认证信息到Security上下文
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username, null, Collections.emptyList()
                                );
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Token即将过期时自动刷新
                        if (jwtUtil.isTokenSoonExpired(token)) {
                            String newToken = jwtUtil.refreshToken(token);
                            response.setHeader(Constants.TOKEN_HEADER, newToken);
                            // 更新Redis中的Token
                            redisUtil.set(redisKey, newToken, Constants.TOKEN_EXPIRE_HOURS,
                                    java.util.concurrent.TimeUnit.HOURS);
                            log.debug("Token已自动刷新: userId={}", userId);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("JWT认证处理异常: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
