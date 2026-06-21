CREATE DATABASE IF NOT EXISTS auth DEFAULT CHARACTER SET utf8mb4;

USE auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL COMMENT '登录名',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt密码',
    display_name VARCHAR(64) COMMENT '显示名',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    lock_until DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    allowed_start_time TIME DEFAULT NULL COMMENT '允许登录开始时间',
    allowed_end_time TIME DEFAULT NULL COMMENT '允许登录结束时间',
    last_login_time DATETIME DEFAULT NULL,
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_username (username)
) COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(256),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(128) NOT NULL COMMENT '权限编码，如 order:query',
    perm_name VARCHAR(64) NOT NULL COMMENT '权限名称',
    perm_type VARCHAR(16) NOT NULL DEFAULT 'API' COMMENT 'MENU/BUTTON/API',
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(256) COMMENT '菜单路径或API路径',
    status INT NOT NULL DEFAULT 1,
    sort_no INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_perm_code (perm_code)
) COMMENT='权限';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT='用户角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) COMMENT='角色权限';

CREATE TABLE IF NOT EXISTS sys_refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_id VARCHAR(64) NOT NULL COMMENT '刷新令牌ID',
    expire_time DATETIME NOT NULL,
    revoked INT NOT NULL DEFAULT 0 COMMENT '1已吊销',
    create_time DATETIME,
    UNIQUE KEY uk_token_id (token_id),
    KEY idx_user_id (user_id)
) COMMENT='刷新令牌';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(64),
    login_ip VARCHAR(64),
    status INT NOT NULL COMMENT '1成功 0失败',
    message VARCHAR(256),
    login_time DATETIME
) COMMENT='登录日志';

INSERT INTO sys_role (role_code, role_name, status, remark, create_time, update_time)
VALUES ('ADMIN', '系统管理员', 1, '拥有全部权限', NOW(), NOW()),
       ('OPERATOR', '运营人员', 1, '业务操作权限', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO sys_permission (perm_code, perm_name, perm_type, parent_id, path, status, sort_no, create_time, update_time)
VALUES
('system:user:query', '用户查询', 'API', 0, '/user/list', 1, 1, NOW(), NOW()),
('system:user:manage', '用户管理', 'API', 0, '/user/**', 1, 2, NOW(), NOW()),
('merchant:query', '商户查询', 'API', 0, '/merchant/**', 1, 10, NOW(), NOW()),
('order:query', '订单查询', 'API', 0, '/order/**', 1, 20, NOW(), NOW()),
('order:manage', '订单管理', 'API', 0, '/order/**', 1, 21, NOW(), NOW()),
('billing:query', '计费查询', 'API', 0, '/billing/**', 1, 30, NOW(), NOW()),
('settle:query', '结算查询', 'API', 0, '/settle/**', 1, 40, NOW(), NOW()),
('reconcile:query', '对账查询', 'API', 0, '/reconcile/**', 1, 50, NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();
