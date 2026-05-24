# common-spring — AGENTS.md

Spring 感知的公共组件模块，依赖 `common` + `spring-boot-starter-web` + `spring-boot-starter-validation`。

业务模块**只需要依赖此模块**即可获取所有 Spring Web / Validation 相关依赖，无需逐个声明。

## 内容

```
filter/
  ├── TraceIdFilter.java           # 请求链路 TraceId 注入（OncePerRequestFilter @Order(1)）
  └── AccessLogFilter.java         # 访问日志记录（OncePerRequestFilter @Order(2)）
advice/
  └── TraceIdResponseAdvice.java   # ApiResponse.traceId 自动填充 + 存储响应码（ResponseBodyAdvice）
exception/
  └── GlobalExceptionHandler.java
```

### TraceIdFilter

`@Component @Order(1)` — 在每个 HTTP 请求入口处：

1. 从 `X-Trace-Id` 请求头读取 traceId（支持上游透传）
2. 不存在则自动生成 16 位 hex 字符串
3. 注入 `MDC.put("traceId", ...)`，供日志 `[%X{traceId}]` 使用
4. 写入 `X-Trace-Id` 响应头
5. 请求结束时 `MDC.remove("traceId")` 防止线程池污染

### TraceIdResponseAdvice

`@ControllerAdvice @Order(1)` — 在所有 Controller + 异常处理器返回 `ApiResponse` 时：
- 自动将 MDC 中的 traceId 写入 `apiResponse.setTraceId()`
- 同时将业务响应码存入请求属性 `_apiResponseCode`，供 `AccessLogFilter` 在响应日志中读取

### AccessLogFilter

`@Component @Order(2)`（在 TraceIdFilter 之后执行）— 记录每次 HTTP 调用：

```
→ GET /api/health | from 127.0.0.1 | agent: curl/8.0
← GET /api/health | status=200 | bizCode=200 12ms
```

在请求入口处记录（方法、URI、客户端 IP、User-Agent），响应返回后记录（状态码、耗时、业务码）。

### GlobalExceptionHandler

`@RestControllerAdvice` 全局异常处理器，覆盖：

| 异常类型 | HTTP 状态 | 说明 |
|----------|:--------:|------|
| `BaseException` | 由 `ErrorCode.getHttpStatus()` 动态决定 | `ResponseEntity` 动态设置 |
| `MethodArgumentNotValidException` | 400 | 请求体校验失败 |
| `BindException` | 400 | 参数绑定失败 |
| `ConstraintViolationException` | 400 | 参数校验 |
| `MissingServletRequestParameterException` | 400 | 缺少请求参数 |
| `MethodArgumentTypeMismatchException` | 400 | 参数类型不匹配 |
| `HttpMessageNotReadableException` | 400 | 请求体格式错误 |
| `HttpRequestMethodNotSupportedException` | 405 | 请求方法不支持 |
| `HttpMediaTypeNotSupportedException` | 415 | 不支持的 MediaType |
| `NoResourceFoundException` | 404 | 资源不存在 |
| `Exception`（兜底） | 500 | 未知异常 |

详细规范参见 [common-infrastructure/spec.md](../openspec/specs/common-infrastructure/spec.md)。

## 依赖说明

```xml
<dependencies>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>common</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```
