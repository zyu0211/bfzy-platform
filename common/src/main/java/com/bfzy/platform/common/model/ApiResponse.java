package com.bfzy.platform.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.bfzy.platform.common.exception.ErrorCode;
import com.bfzy.platform.common.exception.SystemErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 通用 API 响应体.
 * <p>
 * 所有 REST 接口统一使用此结构返回：
 * <pre>
 * {
 *   "code": 200,
 *   "success": true,
 *   "message": "Success",
 *   "data": { ... },
 *   "timestamp": 1712345678000,
 *   "trace_id": "trace-123456789"
 * }
 * </pre>
 * </p>
 *
 * @param <T> data 字段的类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 业务状态码，200 表示成功，其余为错误码
     */
    private int code;

    /**
     * 是否成功（code == 200）
     */
    private boolean success;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 响应时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 链路追踪 ID，用于请求串联与日志检索
     */
    @JsonProperty("trace_id")
    private String traceId;

    // ========== 成功响应 ==========

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(SystemErrorCode.SUCCESS.getCode())
                .success(true)
                .message(SystemErrorCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    // ========== 失败响应 ==========

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .success(code == SystemErrorCode.SUCCESS.getCode())
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}
