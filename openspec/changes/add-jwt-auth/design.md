## Context

BFZY Platform 当前无认证机制，所有请求透传至 Controller。需要引入无状态 JWT 认证，在不引入 Session/Redis 的前提下实现 API 保护。单体部署 + SQLite 场景，不需要 OAuth2 或 SSO 等复杂协议。

## Goals / Non-Goals

**Goals:**
- 用户名+密码注册 → BCrypt 加密落库
- 用户名+密码登录 → 签发 JWT Token
- Bearer Token 请求校验 → 注入 SecurityContext
- 认证失败 → 401 + ApiResponse 格式
- `POST /api/auth/**` 放行，其余默认需认证
- 新模块 `security`，与业务模块解耦

**Non-Goals:**
- 角色/权限体系（后续通过 `@PreAuthorize` 扩展）
- Token 刷新机制（第一阶段 access_token 设较长有效期）
- OAuth2 / 第三方登录
- 用户管理后台

## Decisions

### 1. 新模块 `security` 而非并入 `common-spring`
- **理由**：认证是独立业务领域，有自己的实体（User）、持久化（UserMapper）、配置（SecurityConfig），与 common-spring 的基础设施定位不符
- **替代方案**：放入 common-spring → 会引入 spring-security 污染基础模块
- **结论**：新模块，与其他业务模块同级

### 2. `jjwt` 0.12.x（io.jsonwebtoken）作为 JWT 库
- **理由**：jjwt 是最广泛使用的 Java JWT 库，0.12.x 支持 JWT 解析/校验/签发，API 清晰
- **替代方案**：`com.nimbusds:nimbus-jose-jwt`（Spring 内嵌，但 API 更复杂）；自实现 HMAC → 安全风险
- **结论**：jjwt 0.12.6

### 3. `spring-boot-starter-security` + 自定义 Filter，而非完全无 Security
- **理由**：Spring Security 提供 SecurityContextHolder、方法安全（`@PreAuthorize`）、BCryptPasswordEncoder、CORS 集成，比手写 Filter 更可持续
- **方式**：`SecurityFilterChain` + 自定义 `JwtAuthFilter extends OncePerRequestFilter`
- **结论**：Spring Security 生态，但仅在 security 模块内引入

### 4. Token 存储：无状态（客户端持有），不放 Redis/Session
- **理由**：单体 + 个人项目，无状态最简单；登出由客户端丢弃 Token
- **权衡**：无法强制使 Token 失效（登出、踢人）—— 第一阶段可接受
- **结论**：Access Token 有效期 24 小时，无 refresh token（第一阶段）

### 5. User + UserAuth 双表设计
- **理由**：单 User 表（含 password）无法支持微信/OAuth2 等第三方登录。一个用户需要绑定多种登录方式，且微信登录用户没有密码
- **设计**：
  - `User` 表：核心档案，不存密码。字段：id, username(唯一,nullable), nickname, avatar, email(唯一,nullable), phone(唯一,nullable)
  - `UserAuth` 表：认证方式，一对多。字段：id, userId(FK), identityType(PASSWORD|WECHAT_OPENID|…), identifier(username/openId/email), credential(BCrypt/unionId), 唯一约束(identityType, identifier)
  - Phase 1 密码登录走 UserAuth(PASSWORD, username, BCrypt hash)，username 实际存储到 identifier 字段
- **替代方案**：单 User 表加大量 nullable 字段（password, wechat_openid, qq_openid, github_id...）→ 表宽且冗余
- **结论**：双表设计，后续扩展不加表

### 6. 认证失败响应格式
- **理由**：平台所有接口统一使用 `ApiResponse`，认证失败也应遵守
- **方式**：自定义 `AuthenticationEntryPoint` 返回 `ApiResponse.fail()` + 401
- **结论**：保持前后端一致的响应契约

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 无 refresh token，Token 泄露后有效期长 | 第一阶段接受；后续添加 refresh token + 黑名单机制 |
| SQLite 并发写入 User 表 | 个人项目并发极低，可接受 |
| Spring Security 默认重定向登录页与 REST API 冲突 | 禁用 formLogin，仅使用 filter 机制 |
| jjwt 依赖版本与 Spring Boot 管理的 nimbus-jose-jwt 冲突 | jjwt 与 nimbus 无依赖冲突，可共存 |

