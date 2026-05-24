package com.bfzy.platform.account.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者.
 * <p>
 * 负责 Token 签发、解析、校验。
 * 配置由 {@link JwtProperties} 统一管理。
 * </p>
 *
 * @author zhangyu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public long getExpiration() {
        return jwtProperties.getExpiration();
    }

    public long getRefreshExpiration() {
        return jwtProperties.getRefreshExpiration();
    }

    /**
     * 签发 Access Token.
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT token 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 签发 Refresh Token.
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 校验 Refresh Token 是否有效.
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null && "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中提取用户 ID.
     *
     * @param token JWT token
     * @return 用户 ID，解析失败返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    /**
     * 从 Token 中提取用户名.
     *
     * @param token JWT token
     * @return 用户名，解析失败返回 null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 校验 Token 是否有效.
     *
     * @param token JWT token
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Token 获取 Claims.
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return null;
    }
}
