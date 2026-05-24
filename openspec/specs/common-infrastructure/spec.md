# Common Infrastructure Specification

## Purpose

The `common` module provides pure Java shared infrastructure with zero Spring dependencies: unified API response format, exception hierarchy, error codes, JSON utilities, and page result wrapper. Its sibling `common-spring` module provides Spring-aware global exception handling.

## Requirements

### Requirement: Unified API Response Format

All REST endpoints SHALL return responses wrapped in `ApiResponse<T>` with a consistent JSON structure containing `code`, `success`, `message`, `data`, `timestamp`, and `trace_id`.

#### Scenario: Successful response

- GIVEN a controller method returns `ApiResponse.success(data)`
- WHEN the response is serialized
- THEN the JSON SHALL contain `"code": 200`, `"success": true`, `"message": "Success"`, `"data": {…}`, and `"timestamp": <epoch millis>`, and `"trace_id": "…"`

#### Scenario: Error response

- GIVEN a controller method returns `ApiResponse.fail(errorCode)`
- WHEN the response is serialized
- THEN the JSON SHALL contain the error numeric `code`, `"success": false`, the error `message`, `"data"` SHALL be absent, and `"timestamp"` and `"trace_id"` SHALL be present

#### Scenario: Null data omission

- GIVEN `ApiResponse.success(null)` is returned
- WHEN the response is serialized
- THEN `"data"` SHALL be absent from the JSON (`@JsonInclude(Include.NON_NULL)`)

#### Scenario: Snake-case trace ID

- GIVEN `ApiResponse` has a field named `traceId`
- WHEN the response is serialized
- THEN the JSON key SHALL be `"trace_id"` (via `@JsonProperty("trace_id")`)
- AND the field value SHALL be auto-filled by `TraceIdResponseAdvice` from MDC context

#### Scenario: TraceId auto-fill via ResponseBodyAdvice

- GIVEN `TraceIdResponseAdvice` is registered as a `@ControllerAdvice`
- WHEN any controller or exception handler returns an `ApiResponse`
- THEN `TraceIdResponseAdvice.beforeBodyWrite` SHALL inject the current request's `traceId` from MDC into `apiResponse.setTraceId()`
- AND the developer SHALL NOT need to manually set `traceId`

#### Scenario: TraceId lifecycle via filter

- GIVEN an HTTP request reaches the application
- WHEN `TraceIdFilter.doFilterInternal` executes
- THEN it SHALL check the `X-Trace-Id` request header for upstream trace propagation
- AND if absent, SHALL generate a new 16-character hex trace ID
- AND SHALL set `MDC.put("traceId", traceId)`
- AND SHALL set the response header `X-Trace-Id`
- AND SHALL call `MDC.remove("traceId")` in the `finally` block to prevent thread-pool context pollution

#### Scenario: Log pattern includes traceId

- GIVEN the logging configuration
- WHEN any log statement is written during request processing
- THEN the log pattern SHALL include `[%X{traceId}]` to display the current request's trace ID
- AND every request's log lines SHALL be linkable by their trace ID

#### Scenario: Response code stored for access log

- GIVEN `TraceIdResponseAdvice` processes an `ApiResponse` response
- WHEN the response body contains a `code` field (business code)
- THEN the advice SHALL store that code as a request attribute named `_apiResponseCode`
- AND `AccessLogFilter` SHALL read that attribute in its `finally` block to include it in the response log line

### Requirement: Error Code Interface

All error codes SHALL implement the `ErrorCode` interface providing `int getCode()`, `int getHttpStatus()`, and `String getMessage()`.

#### Scenario: Dual-code structure

- GIVEN an `ErrorCode` implementation
- WHEN `getCode()` is called
- THEN it SHALL return the business-level error code (used in response body `code` field)
- WHEN `getHttpStatus()` is called
- THEN it SHALL return the HTTP response status code (used to set `ResponseEntity` status)
- AND the two values SHALL be independent

#### Scenario: System error codes

- GIVEN a system-level error occurs
- WHEN the error is mapped to an error code
- THEN `SystemErrorCode` SHALL provide standard HTTP-mapped codes from `200` through `504`
- AND `getCode()` and `getHttpStatus()` SHALL return the same value (HTTP status mirrors business code)
- AND `httpStatus` values SHALL use `java.net.HttpURLConnection.HTTP_*` int constants

#### Scenario: Common business error codes

- GIVEN a business validation error occurs
- WHEN the error is mapped to an error code
- THEN `CommonBizCode` SHALL provide common business error codes with business codes in the 10001~19999 range
- AND `getHttpStatus()` SHALL return `HttpURLConnection.HTTP_OK` (200) — HTTP status is always 200 for business errors
- AND the business code in the response body distinguishes error types

#### Scenario: Custom business error codes

- GIVEN a business module needs domain-specific errors
- WHEN defining new error codes
- THEN the module SHALL define an enum implementing `ErrorCode`
- AND codes SHALL NOT conflict with system or common business codes
- AND `getHttpStatus()` SHALL typically return `HttpURLConnection.HTTP_OK` (200) for business logic errors

### Requirement: Exception Hierarchy

All custom exceptions SHALL extend `BaseException`, which carries an error code, message, and HTTP status.

#### Scenario: Base exception

- GIVEN a `BaseException` is constructed with an `ErrorCode`
- WHEN `getCode()`, `getHttpStatus()`, and `getMessage()` are called
- THEN they SHALL reflect the values from the `ErrorCode`
- AND the HTTP status SHALL be accessible via `getHttpStatus()` for response handling

#### Scenario: Business exception

- GIVEN a service method encounters a business rule violation
- WHEN it throws `BizException`
- THEN `GlobalExceptionHandler` SHALL catch it and return an `ApiResponse` with the appropriate HTTP status and business code

### Requirement: Global Exception Handling (common-spring module)

All unhandled exceptions from any module SHALL be caught by `GlobalExceptionHandler` annotated with `@RestControllerAdvice`, located in the `common-spring` module.

#### Scenario: BaseException handling via ResponseEntity

- GIVEN a `BaseException` is thrown
- WHEN `GlobalExceptionHandler.handleBaseException` catches it
- THEN it SHALL return `ResponseEntity.status(HttpStatus.valueOf(e.getHttpStatus())).body(ApiResponse.fail(e.getErrorCode()))`
- AND the HTTP response status SHALL be dynamically set from `ErrorCode.getHttpStatus()`

#### Scenario: Validation error

- GIVEN a request with invalid `@Valid` parameters
- WHEN `MethodArgumentNotValidException` is thrown
- THEN the handler SHALL return status 400 with field-level error messages concatenated

#### Scenario: Unknown error

- GIVEN an unexpected `Exception` is thrown
- WHEN the handler catches it
- THEN it SHALL log the full stack trace at ERROR level
- AND return status 500 with a generic "服务器内部错误" message

### Requirement: Page Result Wrapper

The system SHALL provide a `PageResult<T>` class to wrap paginated query results.

#### Scenario: Page creation

- GIVEN a query returns a list of records, total count, page number, and page size
- WHEN `PageResult.of(records, total, page, pageSize)` is called
- THEN the result SHALL contain the records, total count, current page, page size, and auto-calculated `totalPages`
- AND `PageResult.empty()` SHALL return a result with an empty list and zero counts

### Requirement: Categorized Constants

Constants SHALL be organized into dedicated classes by domain rather than a single monolithic constant class.

#### Scenario: Page constants

- GIVEN `PageConstant` class
- WHEN referencing default pagination values
- THEN `PageConstant.DEFAULT_PAGE` (1), `DEFAULT_PAGE_SIZE` (10), and `MAX_PAGE_SIZE` (1000) SHALL be available

#### Scenario: Date constants

- GIVEN `DateConstant` class
- WHEN referencing date/time format patterns
- THEN `DateConstant.DATE_TIME_FORMAT` ("yyyy-MM-dd HH:mm:ss"), `DATE_FORMAT` ("yyyy-MM-dd"), and `TIME_FORMAT` ("HH:mm:ss") SHALL be available

### Requirement: Hutool Utility Library

The `common` module SHALL depend on `cn.hutool:hutool-all` to provide a comprehensive set of Java utility functions, available transitively to all modules.

#### Scenario: Common utility usage

- GIVEN any module depends on `common` (directly or transitively)
- WHEN the developer needs string, collection, UUID, date, or bean operations
- THEN `hutool-all` SHALL be available on the classpath
- AND the developer SHALL prefer Hutool utilities over hand-written helpers for common operations

### Requirement: JSON Utility

The `common` module SHALL provide a `JsonUtil` class wrapping a pre-configured Jackson `ObjectMapper` with jsr310 support and lenient deserialization.

#### Scenario: Serialize to JSON

- GIVEN any Java object
- WHEN `JsonUtil.toJson(obj)` is called
- THEN it SHALL return a JSON string
- AND throw `RuntimeException` on failure

#### Scenario: Deserialize from JSON

- GIVEN a JSON string and a target class
- WHEN `JsonUtil.fromJson(json, Clazz)` is called
- THEN it SHALL return a populated object
- AND ignore unknown JSON properties

#### Scenario: Typed Map deserialization

- GIVEN a JSON string representing an object
- WHEN `JsonUtil.toMap(json)` is called
- THEN it SHALL return `Map<String, Object>`
- WHEN `JsonUtil.toMap(json, User.class)` is called
- THEN it SHALL return `Map<String, User>` (value type constrained)
- WHEN `JsonUtil.toMap(json, Integer.class, String.class)` is called
- THEN it SHALL return `Map<Integer, String>` (both key and value types constrained)
