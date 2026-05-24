-- BFZY Platform 数据库初始化脚本
-- 由 spring.sql.init.mode=always 自动执行

-- 用户档案表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(64) UNIQUE,
    nickname    VARCHAR(64),
    avatar      VARCHAR(512),
    email       VARCHAR(128) UNIQUE,
    phone       VARCHAR(32) UNIQUE,
    create_time DATETIME,
    update_time DATETIME,
    deleted     TINYINT DEFAULT 0
);

-- 用户认证方式表
CREATE TABLE IF NOT EXISTS sys_user_auth (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES sys_user(id),
    identity_type VARCHAR(32)  NOT NULL,
    identifier    VARCHAR(256) NOT NULL,
    credential    VARCHAR(512),
    create_time   DATETIME,
    update_time   DATETIME,
    deleted       TINYINT DEFAULT 0,
    UNIQUE(identity_type, identifier)
);
