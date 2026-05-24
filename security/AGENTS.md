# security — AGENTS.md

认证与授权模块，提供 JWT 登录、用户管理，以及可扩展的第三方 OAuth2 接入能力。

## 模块依赖

```xml
<dependencies>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>common-spring</artifactId>
    </dependency>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>data</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## 数据库表结构

```sql
-- 用户档案
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(64) UNIQUE,      -- 可为空（纯第三方登录用户）
    nickname    VARCHAR(64),
    avatar      VARCHAR(512),
    email       VARCHAR(128) UNIQUE,
    phone       VARCHAR(32) UNIQUE,
    create_time DATETIME,
    update_time DATETIME,
    deleted     TINYINT DEFAULT 0
);

-- 用户认证方式（一对多）
CREATE TABLE sys_user_auth (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES sys_user(id),
    identity_type VARCHAR(32) NOT NULL,   -- PASSWORD | WECHAT_OPENID | QQ_OPENID | GITHUB | EMAIL | PHONE
    identifier    VARCHAR(256) NOT NULL,  -- username / openId / email
    credential    VARCHAR(512),           -- BCrypt hash / unionId / null
    create_time   DATETIME,
    update_time   DATETIME,
    deleted       TINYINT DEFAULT 0,
    UNIQUE(identity_type, identifier)
);
```

## Phase 1（当前）数据流

```
注册 → User(username=alice) + UserAuth(PASSWORD, alice, BCrypt)
登录 → UserAuth.where(PASSWORD+alice) → 比对 credential → JWT Token
请求 → JwtAuthFilter 解析 Bearer Token → SecurityContextHolder
```

## 新增第三方登录（未来）

只需在 UserAuth 插入一条新记录，不改表结构：

```
微信登录 → UserAuth.where(WECHAT_OPENID+openId) → 查不到则创建 User + UserAuth
```

## 配置说明

```yaml
jwt:
  secret: <256-bit HMAC key>
  expiration: 86400000   # 24小时（毫秒）
```

## 内容

```
src/main/java/com/bfzy/platform/security/
├── config/
│   ├── SecurityConfig.java               # Spring Security 主配置
│   └── AuthenticationEntryPointImpl.java # 认证失败 → 401 + ApiResponse
├── controller/
│   └── AuthController.java               # POST /api/auth/register, /api/auth/login
├── service/
│   ├── AuthService.java                  # 认证服务接口
│   └── impl/
│       └── AuthServiceImpl.java          # 实现：调用 DAO 层，不直接引用 Mapper
├── dao/
│   ├── UserDao.java                      # User DAO 接口（继承 IService<User>）
│   ├── UserAuthDao.java                  # UserAuth DAO 接口（继承 IService<UserAuth>）
│   └── impl/
│       ├── UserDaoImpl.java              # extends ServiceImpl<UserMapper, User>
│       └── UserAuthDaoImpl.java          # extends ServiceImpl<UserAuthMapper, UserAuth>
├── mapper/
│   ├── UserMapper.java                   # User 单表 CRUD（BaseMapper<User>）
│   └── UserAuthMapper.java               # UserAuth 单表 CRUD（BaseMapper<UserAuth>）
├── model/
│   └── vo/
│       ├── request/
│       │   ├── LoginRequest.java         # 登录请求
│       │   └── RegisterRequest.java      # 注册请求
│       └── response/
│           └── LoginResponse.java        # 登录响应（含 Token）
├── filter/
│   └── JwtAuthFilter.java                # OncePerRequestFilter — Bearer Token 校验
├── provider/
│   └── JwtTokenProvider.java             # Token 签发 + 解析 + 校验
└── SecurityCode.java                     # 认证错误码 (implements ErrorCode)
```

### 调用链

```
Controller
  → AuthService（接口）→ AuthServiceImpl（@Service）
    → UserDao / UserAuthDao（接口，继承 IService<Entity>）
      → UserDaoImpl / UserAuthDaoImpl（extends ServiceImpl<Mapper, Entity>）
        → UserMapper / UserAuthMapper（BaseMapper，单表 CRUD）
          → DB
```

### JwtAuthFilter 执行位置

`JwtAuthFilter` 在 Spring Security 过滤器链内部执行（`addFilterBefore(UsernamePasswordAuthenticationFilter.class)`），外层依次为：

```
TraceIdFilter (Order 1) → AccessLogFilter (Order 2) → Spring Security → JwtAuthFilter → Controller
```
