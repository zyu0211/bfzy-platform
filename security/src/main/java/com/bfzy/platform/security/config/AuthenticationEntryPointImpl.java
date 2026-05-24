package com.bfzy.platform.security.config;

import cn.hutool.core.util.StrUtil;
import com.bfzy.platform.common.constant.GlobalConstant;
import com.bfzy.platform.common.exception.SystemErrorCode;
import com.bfzy.platform.common.model.ApiResponse;
import com.bfzy.platform.common.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 自定义认证失败入口.
 * <p>
 * 未认证请求被拒绝时，返回 401 + {@link ApiResponse} 格式，
 * 保持与平台统一的响应契约。
 * </p>
 */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Authentication failed: {} {} | {}", request.getMethod(),
                request.getRequestURI(), authException.getMessage());

        // 从 MDC 读取 traceId（由 TraceIdFilter 注入）
        String traceId = MDC.get(GlobalConstant.Mdc.TRACE_ID);

        ApiResponse<Void> body = ApiResponse.fail(
                SystemErrorCode.UNAUTHORIZED, "认证失败，请先登录");
        if (StrUtil.isNotBlank(traceId)) {
            body.setTraceId(traceId);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtil.toJson(body));
    }
}
