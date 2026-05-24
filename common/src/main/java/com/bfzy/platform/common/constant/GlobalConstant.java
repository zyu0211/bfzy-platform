package com.bfzy.platform.common.constant;

/**
 * 全局常量.
 * <p>
 * 按领域通过内部类分组，避免分散定义造成的重复或不一致。
 * 各业务模块应优先引用此处定义的常量，而非自行定义私有常量或字面量。
 * </p>
 */
public final class GlobalConstant {
    private GlobalConstant() {
    }

    /**
     * HTTP 请求/响应头名
     */
    public static final class Header {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_TRACE_ID = "X-Trace-Id";
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String X_REAL_IP = "X-Real-IP";
        public static final String USER_AGENT = "User-Agent";
    }

    /**
     * 认证 / Token 常量
     */
    public static final class Auth {
        /**
         * Bearer Token 类型标识
         */
        public static final String TOKEN_TYPE_BEARER = "Bearer";
        /**
         * Authorization 头的 Bearer 前缀（含尾部空格，用于解析）
         */
        public static final String BEARER_PREFIX = TOKEN_TYPE_BEARER + " ";
    }

    /**
     * SLF4J MDC 上下文键
     */
    public static final class Mdc {
        public static final String TRACE_ID = "traceId";
    }

    /**
     * 请求属性键
     */
    public static final class RequestAttr {
        public static final String API_RESPONSE_CODE = "_apiResponseCode";
    }
}
