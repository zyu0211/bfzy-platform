package com.bfzy.platform.common.exception;

import java.net.HttpURLConnection;

/**
 * 公共业务错误码枚举.
 * <p>
 * 所有业务模块的错误码统一在此定义，避免跨模块 code 冲突。
 * 通过域注释分块管理，每个域使用独立的 code 段：
 * <pre>
 *   10001~19999  通用业务
 *   20001~29999  认证授权 (security)
 * </pre>
 * </p>
 */
public enum CommonBizCode implements ErrorCode {

    // ========== 通用业务码 (10001~19999) ==========
    PARAM_INVALID(10001, "请求参数校验失败"),
    DATA_NOT_FOUND(10002, "请求数据不存在"),
    OPERATION_FAILED(10003, "操作执行失败"),
    STATE_CONFLICT(10004, "状态冲突"),
    DEPENDENCY_ERROR(10005, "外部依赖调用异常"),

    // ========== 认证授权 (20001~29999) ==========
    TOKEN_INVALID(20001, HttpURLConnection.HTTP_UNAUTHORIZED, "Token 无效或已过期"),
    CREDENTIALS_INVALID(20002, HttpURLConnection.HTTP_UNAUTHORIZED, "用户名或密码错误"),
    USERNAME_DUPLICATE(20003, "用户名已被占用"),
    USERNAME_BLANK(20004, HttpURLConnection.HTTP_BAD_REQUEST, "用户名不能为空"),
    PASSWORD_BLANK(20005, HttpURLConnection.HTTP_BAD_REQUEST, "密码不能为空"),
    PASSWORD_TOO_SHORT(20006, HttpURLConnection.HTTP_BAD_REQUEST, "密码长度不能少于6位"),
    USER_NOT_FOUND(20007, HttpURLConnection.HTTP_UNAUTHORIZED, "用户不存在"),
    ;

    private final int code;
    private final int httpStatus;
    private final String message;

    /**
     * 通用业务错误码构造器（HTTP 状态码默认 200）.
     *
     * @param code    业务码
     * @param message 错误描述
     */
    CommonBizCode(int code, String message) {
        this(code, HttpURLConnection.HTTP_OK, message);
    }

    /**
     * 业务错误码构造器.
     *
     * @param code       业务码
     * @param httpStatus HTTP 响应状态码
     * @param message    错误描述
     */
    CommonBizCode(int code, int httpStatus, String message) {
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
