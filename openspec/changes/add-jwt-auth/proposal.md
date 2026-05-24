## Why

BFZY Platform 目前没有任何认证机制 — 所有 API 端点均可匿名访问。随着业务模块（博客、小程序后端等）逐步建设，需要一套轻量的认证授权体系来保护私有接口。JWT 无状态认证最适合单体部署 + 前后端分离的个人服务平台场景。

## What Changes

- 新增 `security` Maven 模块，作为平台第六个模块
- 引入 `spring-boot-starter-security` + `jjwt` 依赖
- 用户注册接口 `POST /api/auth/register`
- 用户登录接口 `POST /api/auth/login` → 返回 JWT Token
- JWT 请求过滤器，从 `Authorization: Bearer <token>` 解析并校验
- `BCryptPasswordEncoder` 密码加密
- 用户实体 `User`（id, username, password, nickname, createTime, updateTime, deleted）
- 认证失败统一返回 401 + `ApiResponse` 格式
- `POST /api/auth/**` 放行，其余接口需认证

## Capabilities

### New Capabilities
- `security-module`: JWT 认证与授权 — 用户注册、登录、Token 签发与校验、请求认证拦截

### Modified Capabilities
<!-- 无现有 spec 变更 -->
- （无）

## Impact

| 影响范围 | 说明 |
|----------|------|
| 新增模块 | `security/` — Maven 模块，依赖 `common-spring` + `data` |
| 父 POM | `<modules>` 加入 `security`，`<dependencyManagement>` 加入依赖 |
| start 模块 | `pom.xml` 加入对 `security` 的依赖 |
| 现有代码 | 无破坏性变更 |

