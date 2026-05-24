# Account Module Specification

## Purpose

The `account` module provides user authentication and account management for the BFZY Platform. It handles user registration, login, JWT token issuance and validation, token refresh, and profile management. Authentication is designed as stateless JWT — tokens are NOT stored in the database.

## Requirements

### Requirement: User Registration

The system SHALL allow new users to register with a username and password, storing credentials securely using BCrypt password hashing.

#### Scenario: Successful registration

- GIVEN the application is running
- WHEN a client sends `POST /api/auth/register` with a JSON body containing `username` and `password`
- THEN the system SHALL return status 200
- AND the response SHALL be an `ApiResponse` with `success: true` and `data` containing the new user ID
- AND a `UserEntity` record SHALL be created with the given username
- AND a `UserAuthEntity` record SHALL be created with `identityType = PASSWORD`, `identifier = username`, `credential = <BCrypt hash>`

#### Scenario: Duplicate username

- GIVEN a user already exists with username `"alice"`
- WHEN a client sends `POST /api/auth/register` with `username: "alice"`
- THEN the system SHALL return an `ApiResponse` with `success: false` and business code indicating duplicate

#### Scenario: Missing or invalid fields

- GIVEN a registration request
- WHEN `username` or `password` is missing, blank, or password shorter than 6 characters
- THEN the system SHALL return status 400
- AND the response SHALL be an `ApiResponse` with validation error details

### Requirement: User Login

The system SHALL authenticate users by username and password, issuing a signed JWT access token and refresh token upon successful authentication.

#### Scenario: Successful login

- GIVEN a registered user with correct credentials
- WHEN a client sends `POST /api/auth/login` with `username` and `password`
- THEN the system SHALL return status 200
- AND the response SHALL be an `ApiResponse` with `data` containing `token`, `tokenType`, `expiresIn`, `refreshToken`, `refreshExpiresIn`, `userId`, `username`, `nickname`
- AND the token SHALL be a signed JWT containing the user's `id` and `username`

#### Scenario: Invalid credentials

- GIVEN a registered user
- WHEN a client sends `POST /api/auth/login` with an incorrect password
- THEN the system SHALL return status 200 with `success: false`
- AND the response SHALL contain business code indicating invalid credentials

#### Scenario: Non-existent user

- GIVEN no user exists with the given username
- WHEN a client sends `POST /api/auth/login`
- THEN the system SHALL return status 200 with `success: false`
- AND the response SHALL contain business code indicating invalid credentials

### Requirement: Token Refresh

The system SHALL support refresh token rotation for obtaining new access tokens without re-authentication.

#### Scenario: Successful token refresh

- GIVEN a valid refresh token
- WHEN a client sends `POST /api/auth/refresh` with `refreshToken`
- THEN the system SHALL return status 200
- AND the response SHALL contain a new `token` and `refreshToken` pair
- AND the old refresh token SHALL remain valid until natural expiry (soft rotation)

#### Scenario: Invalid or expired refresh token

- GIVEN a malformed or expired refresh token
- WHEN a client sends `POST /api/auth/refresh`
- THEN the system SHALL return status 200 with `success: false`
- AND the response SHALL contain business code `TOKEN_INVALID`

### Requirement: JWT Stateless Authentication

The system SHALL validate incoming API requests by extracting and verifying a JWT Bearer token from the `Authorization` header. Tokens SHALL NOT be stored in the database.

#### Scenario: Valid token

- GIVEN a valid JWT token issued by the system
- WHEN a client sends a request to any protected endpoint with `Authorization: Bearer <token>`
- THEN the request SHALL be authenticated
- AND `SecurityContextHolder` SHALL contain the authenticated user's ID as principal
- AND the controller SHALL process the request normally

#### Scenario: Missing token

- GIVEN no `Authorization` header
- WHEN a client sends a request to a protected endpoint
- THEN the system SHALL return HTTP 401
- AND the response SHALL be an `ApiResponse` with authentication error

#### Scenario: Invalid or expired token

- GIVEN a malformed or expired JWT token
- WHEN a client sends a request with `Authorization: Bearer <invalid_token>`
- THEN the system SHALL return HTTP 401
- AND the response SHALL be an `ApiResponse` with authentication error

### Requirement: Public Endpoint Bypass

The system SHALL allow unauthenticated access to authentication and health endpoints.

#### Scenario: Auth endpoints are public

- GIVEN no authentication credentials
- WHEN a client sends a request to `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`
- THEN the request SHALL be processed without authentication

#### Scenario: Health and public endpoints are public

- GIVEN no authentication credentials
- WHEN a client sends a request to `GET /api/health`, `GET /api/hello`
- THEN the request SHALL be processed without authentication

### Requirement: User Entity

The system SHALL provide a `UserEntity` representing user profile information, mapped to a `sys_user` database table, extending `BaseEntity` for automatic audit fields. The entity SHALL NOT store authentication credentials directly.

#### Scenario: Entity fields

- GIVEN the `UserEntity` class
- WHEN inspecting its fields
- THEN it SHALL extend `BaseEntity` (inheriting `id`, `createTime`, `updateTime`, `deleted`)
- AND it SHALL have `username` (unique, nullable — a user may only have third-party auth)
- AND it SHALL have `nickname` (nullable)
- AND it SHALL have `avatar` (nullable, URL string)
- AND it SHALL have `email` (unique, nullable)
- AND it SHALL have `phone` (unique, nullable)

### Requirement: UserAuth Entity

The system SHALL provide a `UserAuthEntity` representing an authentication method bound to a user, mapped to a `sys_user_auth` database table. A user SHALL be able to have multiple auth methods (password, WeChat, email, etc.).

#### Scenario: Entity fields

- GIVEN the `UserAuthEntity` class
- WHEN inspecting its fields
- THEN it SHALL extend `BaseEntity`
- AND it SHALL have `userId` (foreign key to `UserEntity.id`, non-nullable)
- AND it SHALL have `identityType` (non-nullable enum: `PASSWORD`, `WECHAT_OPENID`, `QQ_OPENID`, `GITHUB`, `EMAIL`, `PHONE`)
- AND it SHALL have `identifier` (non-nullable string — username for PASSWORD, openId for WECHAT)
- AND it SHALL have `credential` (nullable — BCrypt hash for PASSWORD)

#### Scenario: Unique constraint

- GIVEN the `sys_user_auth` table
- WHEN inserting a record
- THEN the combination of `identity_type` and `identifier` SHALL be unique
- AND this prevents the same identity from being bound to multiple users

### Requirement: JWT Configuration

The system SHALL externalize JWT configuration via `application.yml` using a `JwtProperties` class annotated with `@ConfigurationProperties(prefix = "jwt")`.

#### Scenario: Configurable properties

- GIVEN `application.yml` contains `jwt.*` properties
- WHEN the application starts
- THEN `jwt.secret` SHALL be the HMAC signing key (minimum 256-bit)
- AND `jwt.expiration` SHALL be the access token lifetime in milliseconds (default: 86400000 / 24h)
- AND `jwt.refresh-expiration` SHALL be the refresh token lifetime in milliseconds (default: 604800000 / 7d)

### Requirement: Unified Error Response

Authentication failures SHALL conform to the platform's standard `ApiResponse` format with appropriate HTTP status codes.

#### Scenario: Authentication error format

- GIVEN an authentication failure (missing/invalid/expired token)
- WHEN the system rejects the request
- THEN the response SHALL be `ApiResponse` with the appropriate error code and message
- AND the HTTP status SHALL be 401 (Unauthorized)

## Module Structure

```
account/
├── pom.xml
└── src/main/java/com/bfzy/platform/account/
    ├── auth/                                 # Security configuration
    │   ├── SecurityConfig.java               # SecurityFilterChain + whitelist
    │   ├── AuthenticationEntryPointImpl.java # 401 + ApiResponse
    │   ├── JwtAuthFilter.java                # Bearer Token extraction + validation
    │   ├── JwtTokenProvider.java             # Token signing, parsing, validation
    │   └── JwtProperties.java               # @ConfigurationProperties(prefix = "jwt")
    ├── controller/
    │   ├── AuthController.java               # POST /api/auth/{register,login,refresh}
    │   └── AccountController.java            # GET/PUT /api/account/{profile,password}
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

Entity classes reside in `data` module at `com.bfzy.platform.data.model.security/`:

```
data/src/main/java/com/bfzy/platform/data/model/security/
├── UserEntity.java
└── UserAuthEntity.java
```

## Filter Chain Order

```
TraceIdFilter (Order HIGHEST)
  → AccessLogFilter
    → FilterChainProxy (Spring Security)
      → JwtAuthFilter
        → Controller
```

## Token Format

- **Algorithm**: HMAC-SHA256 (via jjwt)
- **Claims**: `sub` = userId, `username` = username, `type` = "refresh" (for refresh tokens), `iat`, `exp`
- **Storage**: Client-side only (stateless, not persisted in database)
- **Refresh Strategy**: Soft rotation — old refresh token remains valid until expiry
