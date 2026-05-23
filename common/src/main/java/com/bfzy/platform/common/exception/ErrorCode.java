package com.bfzy.platform.common.exception;

/**
 * 错误码接口.
 * <p>
 * 所有错误码枚举应实现此接口，确保整个项目的错误码结构统一。
 * </p>
 */
public interface ErrorCode {

    /**
     * 业务错误码（响应体 {@code code} 字段）
     */
    int getCode();

    /**
     * HTTP 状态码（响应头 {@code status}，值来自 {@link java.net.HttpURLConnection} 常量）
     */
    int getHttpStatus();

    /**
     * 错误描述
     */
    String getMessage();
}
