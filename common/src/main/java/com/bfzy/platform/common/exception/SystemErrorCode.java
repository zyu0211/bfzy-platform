package com.bfzy.platform.common.exception;

import java.net.HttpURLConnection;

/**
 * 系统级错误码枚举.
 * <p>
 * 涵盖 HTTP 标准化状态码映射：{@link #getCode()} 返回响应体中的业务码，
 * {@link #getHttpStatus()} 返回对应的 HTTP 响应状态码（使用 {@link HttpURLConnection} 常量）。
 * </p>
 * <p>
 * 各业务模块可自行定义业务错误码枚举并实现 {@link ErrorCode} 接口。
 * </p>
 */
public enum SystemErrorCode implements ErrorCode {

    SUCCESS(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_OK, "Success"),
    BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request"),
    UNAUTHORIZED(HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND, "Not Found"),
    METHOD_NOT_ALLOWED(HttpURLConnection.HTTP_BAD_METHOD, HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed"),
    CONFLICT(HttpURLConnection.HTTP_CONFLICT, HttpURLConnection.HTTP_CONFLICT, "Conflict"),
    UNSUPPORTED_MEDIA_TYPE(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "Unsupported Media Type"),
    TOO_MANY_REQUESTS(429, 429, "Too Many Requests"),
    INTERNAL_ERROR(HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error"),
    SERVICE_UNAVAILABLE(HttpURLConnection.HTTP_UNAVAILABLE, HttpURLConnection.HTTP_UNAVAILABLE, "Service Unavailable"),
    GATEWAY_TIMEOUT(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, HttpURLConnection.HTTP_GATEWAY_TIMEOUT, "Gateway Timeout"),
    ;

    /**
     * 业务错误码（响应体 code）
     */
    private final int code;
    /**
     * HTTP 状态码
     */
    private final int httpStatus;
    private final String message;

    SystemErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
