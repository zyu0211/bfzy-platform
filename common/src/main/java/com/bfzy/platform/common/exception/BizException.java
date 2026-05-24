package com.bfzy.platform.common.exception;

/**
 * 业务异常.
 * <p>
 * 用于业务逻辑校验失败等场景，推荐优先使用带 {@link ErrorCode} 的构造方法，
 * 以便全局异常处理器能精确返回对应的错误码。
 * </p>
 *
 * @author zhangyu
 */
public class BizException extends BaseException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BizException(String message) {
        super(SystemErrorCode.INTERNAL_ERROR, message);
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
