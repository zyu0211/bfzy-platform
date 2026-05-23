package com.bfzy.platform.common.exception;

import java.net.HttpURLConnection;

/**
 * 公共业务错误码枚举.
 * <p>
 * 业务错误码范围约定：{@code 10001 ~ 19999}，HTTP 状态码固定为 {@code 200}，
 * 各业务模块可自行定义各自的业务错误码枚举。
 * </p>
 */
public enum CommonBizCode implements ErrorCode {

    PARAM_INVALID(10001, "请求参数校验失败"),
    DATA_NOT_FOUND(10002, "请求数据不存在"),
    OPERATION_FAILED(10003, "操作执行失败"),
    STATE_CONFLICT(10004, "状态冲突"),
    DEPENDENCY_ERROR(10005, "外部依赖调用异常"),
    ;

    private final int code;
    private final String message;

    CommonBizCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
