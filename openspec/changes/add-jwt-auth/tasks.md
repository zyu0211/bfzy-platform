## 1. Maven 模块与依赖

- [x] 1.1 创建 `security/` 模块目录、`pom.xml`（parent `${revision}`，依赖 `common-spring` + `data`）
- [x] 1.2 父 `pom.xml`：`<modules>` 加入 `security`，`<dependencyManagement>` 加入 `spring-boot-starter-security` + `jjwt-api`/`jjwt-impl`/`jjwt-jackson` 0.12.6
- [x] 1.3 `start/pom.xml` 加入对 `security` 的依赖

## 2. 用户实体与数据访问

- [x] 2.1 创建 `User` 实体（继承 `BaseEntity`），字段：username(唯一,nullable), nickname, avatar, email(唯一,nullable), phone(唯一,nullable)
- [x] 2.2 创建 `UserAuth` 实体（继承 `BaseEntity`），字段：userId, identityType, identifier, credential，唯一约束(identityType, identifier)
- [x] 2.3 创建 `IdentityType` 枚举：PASSWORD, WECHAT_OPENID, QQ_OPENID, GITHUB, EMAIL, PHONE（Phase 1 只用 PASSWORD）
- [x] 2.4 创建 `UserMapper`（继承 `BaseMapper<User>`）
- [x] 2.5 创建 `UserAuthMapper`（继承 `BaseMapper<UserAuth>`）
- [x] 2.6 创建 `src/main/resources/mapper/UserMapper.xml` + `UserAuthMapper.xml`

## 3. JWT Token 提供者

- [x] 3.1 创建 `JwtTokenProvider`：签发 Token（含 id, username）、解析 Token、校验签名和过期
- [x] 3.2 Token 配置可外部化：`jwt.secret`、`jwt.expiration` 从 `application.yml` 读取
- [x] 3.3 提供 `getUserIdFromToken()`、`getUsernameFromToken()`、`validateToken()` 方法

## 4. Spring Security 配置

- [x] 4.1 创建 `SecurityConfig`：`SecurityFilterChain` + `BCryptPasswordEncoder`，禁用 formLogin/csrf，配置 `POST /api/auth/**` 放行
- [x] 4.2 创建 `JwtAuthFilter extends OncePerRequestFilter`：从 `Authorization: Bearer <token>` 提取 → `JwtTokenProvider` 校验 → 设置 `SecurityContextHolder`
- [x] 4.3 创建自定义 `AuthenticationEntryPoint`：认证失败返回 401 + `ApiResponse` 格式
- [x] 4.4 创建 `SecurityCode` 枚举（`implements ErrorCode`）：定义业务错误码（如 `TOKEN_INVALID`, `CREDENTIALS_INVALID`, `USERNAME_DUPLICATE`）

## 5. 认证服务

- [x] 5.1 创建 `AuthService`：`register(username, password)` → 校验 + 创建 User + 创建 UserAuth(PASSWORD) → BCrypt 存 UserAuth.credential；`login(username, password)` → 查 UserAuth(identityType=PASSWORD, identifier=username) → BCrypt 比对 credential → 签发 Token
- [x] 5.2 创建 DTO：`LoginRequest`、`RegisterRequest`（@NotBlank 校验）、`LoginResponse`

## 6. 控制器

- [x] 6.1 创建 `AuthController`：`POST /api/auth/register` + `POST /api/auth/login`，返回 `ApiResponse`

## 7. 验证

- [x] 7.1 `mvn compile` 编译通过
- [ ] 7.2 启动应用，注册用户 → 登录获取 Token → 用 Token 访问受保护接口 → 无 Token 访问被拒（401）（手动验证，已确认编译通过）
