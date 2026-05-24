package com.bfzy.platform.common.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.bfzy.platform.common.constant.GlobalConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceId 过滤器.
 * <p>
 * 在每个 HTTP 请求的入口处：
 * <ul>
 *   <li>从 {@code X-Trace-Id} 请求头中读取 traceId（上游透传）</li>
 *   <li>如果不存在则自动生成</li>
 *   <li>将 traceId 注入 SLF4J MDC，供日志模式 {@code [%X{traceId}]} 使用</li>
 *   <li>将 traceId 写入响应头 {@code X-Trace-Id}，便于客户端/下游透传</li>
 *   <li>请求结束时从 MDC 移除，防止线程池复用导致上下文污染</li>
 * </ul>
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final int TRACE_ID_LENGTH = 16;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. 从请求头获取 traceId（支持上游透传），不存在则生成
        String traceId = request.getHeader(GlobalConstant.Header.X_TRACE_ID);
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID().substring(0, TRACE_ID_LENGTH);
        }

        // 2. 注入 MDC
        MDC.put(GlobalConstant.Mdc.TRACE_ID, traceId);

        // 3. 写入响应头
        response.setHeader(GlobalConstant.Header.X_TRACE_ID, traceId);

        try {
            // 4. 继续过滤器链
            chain.doFilter(request, response);
        } finally {
            // 5. 清理 MDC（防止线程池复用导致上下文污染）
            MDC.remove(GlobalConstant.Mdc.TRACE_ID);
        }
    }
}
