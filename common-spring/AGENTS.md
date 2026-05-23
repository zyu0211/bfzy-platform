# common-spring — AGENTS.md

Spring 感知的公共组件模块，依赖 `common` + `spring-boot-starter-web` + `spring-boot-starter-validation`。

业务模块**只需要依赖此模块**即可获取所有 Spring Web / Validation 相关依赖，无需逐个声明。

## 内容

```
exception/
  └── GlobalExceptionHandler.java
```

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
