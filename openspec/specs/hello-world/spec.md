# Hello World Specification

## Purpose

The `hello-world` module provides the health check and welcome endpoints for the BFZY Platform. It serves as the simplest reference for adding new business modules.

## Requirements

### Requirement: Health Check Endpoint

The system SHALL expose a `GET /api/health` endpoint that returns the current service status, including service name and current timestamp.

#### Scenario: Health check success

- GIVEN the application is running
- WHEN a client sends `GET /api/health`
- THEN the response SHALL have status 200
- AND the body SHALL be an `ApiResponse` with `data.status` equal to `"UP"`
- AND `data.service` SHALL equal `"BFZY Platform"`
- AND `data.timestamp` SHALL be the current ISO-8601 time

### Requirement: Welcome Endpoint

The system SHALL expose a `GET /api/hello` endpoint that returns a welcome greeting message.

#### Scenario: Welcome message returned

- GIVEN the application is running
- WHEN a client sends `GET /api/hello`
- THEN the response SHALL have status 200
- AND the body SHALL be an `ApiResponse` with `data.message` equal to `"Hello, World!"`
- AND `data.greeting` SHALL equal `"Welcome to BFZY Platform"`

### Requirement: New Module Scaffold Reference

The `hello-world` module SHALL serve as the minimal reference for creating new business modules, demonstrating module POM setup, package convention, and controller registration.

#### Scenario: Minimal module structure

- GIVEN a developer wants to create a new module
- WHEN they examine `hello-world`
- THEN they SHALL see: a `pom.xml` with `com.bfzy.platform:common-spring` and `com.bfzy.platform:data` as the standard dependencies, and a `controller` package under `com.bfzy.platform.helloworld.controller`
- AND no `@SpringBootApplication`, no `application.yml` SHALL exist in the module itself

#### Scenario: Dependency pattern

- GIVEN `hello-world/pom.xml`
- WHEN inspecting its dependencies
- THEN `common-spring` SHALL provide transitive access to `common` + `spring-boot-starter-web` + `spring-boot-starter-validation`
- AND `data` SHALL provide transitive access to `mybatis-plus-spring-boot3-starter` + `BaseEntity`
- AND both SHALL omit `<version>` (managed by parent `<dependencyManagement>`)
