# account — AGENTS.md

账号模块，提供注册/登录/JWT 认证/Token 刷新/账号资料管理，以及可扩展的第三方 OAuth2 接入能力。

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

## API 数据流

```
┌─ 公开接口 ─────────────────────────────┐
│ POST /api/auth/register   用户注册       │
│ POST /api/auth/login      登录           │
│ POST /api/auth/refresh    Token 换发    │
└────────────────────────────────────────┘

┌─ 需认证接口 ───────────────────────────┐
│ GET    /api/account/profile  查资料    │
│ PUT    /api/account/profile  改资料    │
│ PUT    /api/account/password 改密码    │
└────────────────────────────────────────┘
```

## 配置说明

```yaml
jwt:
  secret: <256-bit HMAC key>
  expiration: 86400000          # Access Token：24小时（毫秒）
  refresh-expiration: 604800000  # Refresh Token：7天（毫秒）
```

## 内容

```
src/main/java/com/bfzy/platform/account/
├── auth/                                   # 安全认证统一管理
│   ├── SecurityConfig.java                 # Spring Security 主配置
│   ├── AuthenticationEntryPointImpl.java   # 401 + ApiResponse
│   ├── JwtAuthFilter.java                  # Bearer Token 校验
│   └── JwtTokenProvider.java               # Token 签发 + 解析 + 校验
├── controller/
│   ├── AuthController.java                 # 注册/登录/刷新
│   └── AccountController.java              # 资料查询/修改/改密
├── service/
│   ├── AuthService.java → impl/
│   └── AccountService.java → impl/
├── dao/
│   ├── UserDao.java → impl/
│   └── UserAuthDao.java → impl/
├── mapper/
│   ├── UserMapper.java
│   └── UserAuthMapper.java
└── model/vo/
    ├── request/
    │   ├── LoginRequest.java
    │   ├── RegisterRequest.java
    │   ├── RefreshTokenRequest.java
    │   ├── UpdateProfileRequest.java
    │   └── UpdatePasswordRequest.java
    └── response/
        ├── LoginResponse.java
        └── AccountProfileResponse.java
```

### 调用链

```
AuthController / AccountController
  → AuthService / AccountService（接口）
    → AuthServiceImpl / AccountServiceImpl（@Service）
      → UserDao / UserAuthDao（接口，继承 IService<Entity>）
        → UserDaoImpl / UserAuthDaoImpl（extends ServiceImpl<Mapper, Entity>）
          → UserMapper / UserAuthMapper（BaseMapper，单表 CRUD）
            → DB
```

### 过滤器执行位置

```
TraceIdFilter (Order HIGHEST) → AccessLogFilter → FilterChainProxy (Security)
  → JwtAuthFilter → Controller
```
