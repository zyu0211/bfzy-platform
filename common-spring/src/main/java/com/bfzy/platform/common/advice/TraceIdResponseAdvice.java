package com.bfzy.platform.common.advice;

import cn.hutool.core.util.StrUtil;
import com.bfzy.platform.common.constant.GlobalConstant;
import com.bfzy.platform.common.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * TraceId 响应增强器.
 * <p>
 * 自动将 MDC 中的 {@code traceId} 填充到每个 {@link ApiResponse} 的 {@code traceId} 字段，
 * 同时将业务响应码存入请求属性，供 {@code AccessLogFilter} 在响应日志中输出。
 * </p>
 *
 * @author zhangyu
 */
@ControllerAdvice
@Order(1)
public class TraceIdResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> apiResponse) {
            // 填充 traceId
            String traceId = MDC.get(GlobalConstant.Mdc.TRACE_ID);
            if (StrUtil.isNotBlank(traceId)) {
                apiResponse.setTraceId(traceId);
            }
            // 存储业务码供 AccessLogFilter 读取
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                attrs.getRequest().setAttribute(GlobalConstant.RequestAttr.API_RESPONSE_CODE, apiResponse.getCode());
            }
        }
        return body;
    }
}
