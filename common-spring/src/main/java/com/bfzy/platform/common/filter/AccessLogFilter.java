package com.bfzy.platform.common.filter;

import cn.hutool.core.util.StrUtil;

import com.bfzy.platform.common.constant.GlobalConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 访问日志过滤器.
 * <p>
 * 记录每次 HTTP 请求的入口与出口信息：
 * <ul>
 *   <li>请求：HTTP 方法、URI、查询参数、客户端 IP、User-Agent</li>
 *   <li>响应：HTTP 状态码、耗时、业务响应码（从 {@code TraceIdResponseAdvice} 存入的请求属性读取）</li>
 * </ul>
 * </p>
 *
 * @author zhangyu
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AccessLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // 记录请求信息
        log.info("→ {} {} | from {} | agent: {}",
                request.getMethod(),
                getRequestUri(request),
                getClientIp(request),
                StrUtil.blankToDefault(request.getHeader(GlobalConstant.Header.USER_AGENT), "-"));

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 从请求属性读取业务码（由 TraceIdResponseAdvice 写入）
            Object codeAttr = request.getAttribute(GlobalConstant.RequestAttr.API_RESPONSE_CODE);
            String bizCode = (codeAttr != null) ? "" + codeAttr : "";

            log.info("← {} {} | status={} | bizCode={} | cost={}ms",
                    request.getMethod(),
                    getRequestUri(request),
                    response.getStatus(),
                    bizCode.isEmpty() ? "" : bizCode,
                    duration);
        }
    }

    /**
     * 获取完整的请求 URI（含查询参数）.
     */
    private static String getRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String qs = request.getQueryString();
        return (qs != null) ? uri + "?" + qs : uri;
    }

    /**
     * 获取客户端真实 IP，优先取代理透传的请求头.
     */
    private static String getClientIp(HttpServletRequest request) {
        // 1. X-Forwarded-For（Nginx / 反向代理）
        String ip = request.getHeader(GlobalConstant.Header.X_FORWARDED_FOR);
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        // 2. X-Real-IP
        ip = request.getHeader(GlobalConstant.Header.X_REAL_IP);
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        // 3. 直连 IP
        ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
