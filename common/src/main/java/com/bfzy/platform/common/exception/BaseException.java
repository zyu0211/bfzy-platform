package com.bfzy.platform.common.exception;

import lombok.Getter;

import java.net.HttpURLConnection;

/**
 * 基础异常类.
 * <p>
 * 所有自定义异常的根父类，携带错误码与 HTTP 状态码，便于全局异常处理器统一捕获并返回结构化的 {@code ApiResponse}。
 * </p>
 *
 * @author zhangyu
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final int code;

    /**
     * HTTP 状态码
     */
    private final int httpStatus;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
