# Security Module Specification

## Purpose

The `security` module provides JWT-based authentication for the BFZY Platform. It manages user registration, login, token issuance, and request authentication via Spring Security filter chain.

## Requirements

### Requirement: User Registration

The system SHALL allow new users to register with a username and password, storing credentials securely using BCrypt password hashing.

#### Scenario: Successful registration

- GIVEN the application is running
- WHEN a client sends `POST /api/auth/register` with a JSON body containing `username` and `password`
- THEN the system SHALL return status 200
- AND the response SHALL be an `ApiResponse` with `success: true`
- AND the password SHALL be stored hashed with BCrypt (never in plain text)
- AND the user SHALL be created with a unique ID, current timestamps, and `deleted = 0`

#### Scenario: Duplicate username

- GIVEN a user already exists with username `"alice"`
- WHEN a client sends `POST /api/auth/register` with `username: "alice"`
- THEN the system SHALL return status 200 with `success: false`
- AND the response SHALL contain business code indicating conflict/duplicate

#### Scenario: Missing or invalid fields

- GIVEN a registration request
- WHEN `username` or `password` is missing, blank, or too short
- THEN the system SHALL return status 400
- AND the response SHALL be an `ApiResponse` with validation error details

### Requirement: User Login

The system SHALL authenticate users by username and password, issuing a signed JWT access token upon successful authentication.

#### Scenario: Successful login

- GIVEN a registered user with correct credentials
- WHEN a client sends `POST /api/auth/login` with `username` and `password`
- THEN the system SHALL return status 200
- AND the response SHALL be an `ApiResponse` containing a `token` field in `data`
- AND the token SHALL be a signed JWT containing the user's `id` and `username`

#### Scenario: Invalid credentials

- GIVEN a registered user
- WHEN a client sends `POST /api/auth/login` with an incorrect password
- THEN the system SHALL return status 401
- AND the response SHALL be an `ApiResponse` with error message indicating invalid credentials

#### Scenario: Non-existent user

- GIVEN no user exists with the given username
- WHEN a client sends `POST /api/auth/login`
- THEN the system SHALL return status 401
- AND the response SHALL be an `ApiResponse` with error message indicating invalid credentials

### Requirement: JWT Token Authentication

The system SHALL validate incoming API requests by extracting and verifying a JWT Bearer token from the `Authorization` header.

#### Scenario: Valid token

- GIVEN a valid JWT token issued by the system
- WHEN a client sends a request to any protected endpoint with `Authorization: Bearer <token>`
- THEN the request SHALL be authenticated
- AND `SecurityContextHolder` SHALL contain the authenticated user's details
- AND the controller SHALL process the request normally

#### Scenario: Missing token

- GIVEN no `Authorization` header
- WHEN a client sends a request to a protected endpoint
- THEN the system SHALL return status 401
- AND the response SHALL be an `ApiResponse` with authentication error

#### Scenario: Invalid or expired token

- GIVEN a malformed or expired JWT token
- WHEN a client sends a request with `Authorization: Bearer <invalid_token>`
- THEN the system SHALL return status 401
- AND the response SHALL be an `ApiResponse` with authentication error

### Requirement: Public Endpoint Bypass

The system SHALL allow unauthenticated access to authentication endpoints.

#### Scenario: Auth endpoints are public

- GIVEN no authentication credentials
- WHEN a client sends a request to `POST /api/auth/register` or `POST /api/auth/login`
- THEN the request SHALL be processed without authentication
- AND the controller SHALL execute normally

### Requirement: User Entity

The system SHALL provide a `User` entity representing user profile information, mapped to a `sys_user` database table, extending `BaseEntity` for automatic audit fields. The User entity SHALL NOT store authentication credentials directly.

#### Scenario: Entity fields

- GIVEN the `User` entity
- WHEN inspecting its fields
- THEN it SHALL extend `data` module's `BaseEntity` (inheriting `id`, `createTime`, `updateTime`, `deleted`)
- AND it SHALL have `username` (unique, nullable — a user may only have third-party auth)
- AND it SHALL have `nickname` (nullable)
- AND it SHALL have `avatar` (nullable, URL string)
- AND it SHALL have `email` (unique, nullable)
- AND it SHALL have `phone` (unique, nullable)

### Requirement: UserAuth Entity

The system SHALL provide a `UserAuth` entity representing an authentication method bound to a user, mapped to a `sys_user_auth` database table. A user SHALL be able to have multiple auth methods (password, WeChat, email, etc.).

#### Scenario: Entity fields

- GIVEN the `UserAuth` entity
- WHEN inspecting its fields
- THEN it SHALL extend `BaseEntity` (inheriting `id`, `createTime`, `updateTime`, `deleted`)
- AND it SHALL have `userId` (foreign key to `User.id`, non-nullable)
- AND it SHALL have `identityType` (non-nullable string, e.g. `PASSWORD`, `WECHAT_OPENID`, `QQ_OPENID`, `GITHUB`, `EMAIL`, `PHONE`)
- AND it SHALL have `identifier` (non-nullable string — username for PASSWORD, openId for WECHAT, email for EMAIL)
- AND it SHALL have `credential` (nullable — BCrypt hash for PASSWORD, unionId or empty for OAuth2)

#### Scenario: Unique constraint

- GIVEN the `sys_user_auth` table
- WHEN inserting a record
- THEN the combination of `identity_type` and `identifier` SHALL be unique
- AND this prevents the same third-party account from being bound to multiple users

#### Scenario: Registration creates User + UserAuth

- GIVEN username `"alice"` and password `"securePass1!"`
- WHEN a client sends `POST /api/auth/register`
- THEN a new `User` record SHALL be created with `username = "alice"`
- AND a new `UserAuth` record SHALL be created with `identityType = "PASSWORD"`, `identifier = "alice"`, `credential = <BCrypt hash>`

#### Scenario: Login queries UserAuth

- GIVEN a registered user
- WHEN a client sends `POST /api/auth/login` with `username = "alice"` and `password`
- THEN the system SHALL query `UserAuth` where `identityType = "PASSWORD"` and `identifier = "alice"`
- AND verify the password against the stored `credential` (BCrypt)

### Requirement: Unified Error Response

Authentication failures SHALL conform to the platform's standard `ApiResponse` format with appropriate HTTP status codes.

#### Scenario: Authentication error format

- GIVEN an authentication failure (missing/invalid/expired token)
- WHEN the system rejects the request
- THEN the response SHALL be `ApiResponse` with the appropriate error code and message
- AND the HTTP status SHALL be 401 (Unauthorized)

## Module Structure

```
security/src/main/java/com/bfzy/platform/security/
├── config/
│   └── SecurityConfig.java            # Spring Security filter chain + password encoder
├── controller/
│   └── AuthController.java            # POST /api/auth/register, POST /api/auth/login
├── service/
│   ├── AuthService.java               # Register + login business logic (User + UserAuth)
│   └── UserDetailsServiceImpl.java    # Load user for authentication
├── mapper/
│   ├── UserMapper.java                # MyBatis-Plus CRUD for User
│   └── UserAuthMapper.java            # MyBatis-Plus CRUD for UserAuth
├── model/
│   ├── dto/
│   │   ├── LoginRequest.java          # username + password
│   │   ├── RegisterRequest.java       # username + password
│   │   └── LoginResponse.java         # token + user info
│   └── entity/
│       ├── User.java                  # 用户档案（继承 BaseEntity）
│       └── UserAuth.java              # 认证方式（继承 BaseEntity）
├── enums/
│   └── IdentityType.java              # PASSWORD, WECHAT_OPENID, QQ_OPENID, GITHUB, EMAIL, PHONE ...
├── filter/
│   └── JwtAuthFilter.java             # OncePerRequestFilter — Bearer Token 解析与校验
├── provider/
│   └── JwtTokenProvider.java          # Token 签发 + 解析 + 校验
└── SecurityCode.java                  # 业务错误码 (implements ErrorCode)
```
```
