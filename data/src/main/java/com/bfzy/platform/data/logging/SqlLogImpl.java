package com.bfzy.platform.data.logging;

import org.apache.ibatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MyBatis SQL 日志适配器.
 * <p>
 * 将所有 MyBatis 执行日志统一路由到 {@code SQL} 这个 logger 名下，
 * 与业务代码的 DEBUG 日志彻底隔离。在 logback-spring.xml 中通过
 * {@code <logger name="SQL" level="DEBUG">} 独立输出到 SQL_FILE。
 * </p>
 *
 * <p>配置方式（application-prod.yml）：</p>
 * <pre>{@code
 * mybatis-plus:
 *   configuration:
 *     log-impl: com.bfzy.platform.data.logging.SqlLogImpl
 * }</pre>
 *
 * @author zhangyu
 */
public class SqlLogImpl implements Log {

    private final Logger log;

    /**
     * MyBatis 会在实例化时传入 mapper 接口的全限定名，
     * 此处统一收归到 {@code "SQL"} logger，不管实际是哪个 mapper。
     */
    public SqlLogImpl(String clazz) {
        this.log = LoggerFactory.getLogger("SQL");
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }
}
