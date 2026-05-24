package com.bfzy.platform.account.auth;

import cn.hutool.core.util.StrUtil;
import com.bfzy.platform.common.constant.GlobalConstant;
import com.bfzy.platform.account.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器.
 * <p>
 * 从 {@code Authorization: Bearer <token>} 头中提取 Token，
 * 校验通过后设置 {@code SecurityContextHolder}，后续请求即可获取用户信息。
 * </p>
 *
 * @author zhangyu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StrUtil.isNotBlank(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            if (userId != null && username != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {} (id={})", username, userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer Token.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(GlobalConstant.Header.AUTHORIZATION);
        if (StrUtil.isNotBlank(header) && header.startsWith(GlobalConstant.Auth.BEARER_PREFIX)) {
            return header.substring(GlobalConstant.Auth.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
