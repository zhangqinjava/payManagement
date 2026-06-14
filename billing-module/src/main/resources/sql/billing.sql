CREATE DATABASE IF NOT EXISTS billing DEFAULT CHARACTER SET utf8mb4;

USE billing;

CREATE TABLE IF NOT EXISTS billing_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(32) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(64) NOT NULL COMMENT '模板名称',
    merchant_type VARCHAR(16) NOT NULL DEFAULT 'DEFAULT' COMMENT '商户类型，DEFAULT表示通用',
    biz_type INT NOT NULL COMMENT '0付款 1退款 2收单',
    fee_mode INT NOT NULL COMMENT '1比例 2固定 3混合 4梯度',
    fee_type VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '扣款方式',
    rate DECIMAL(10,6) DEFAULT 0 COMMENT '费率',
    fixed_fee DECIMAL(18,2) DEFAULT 0 COMMENT '固定手续费',
    min_fee DECIMAL(18,2) DEFAULT 0 COMMENT '最低手续费',
    max_fee DECIMAL(18,2) DEFAULT 0 COMMENT '最高手续费',
    currency VARCHAR(8) NOT NULL DEFAULT 'rmb' COMMENT '币种',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark VARCHAR(256),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_template_code (template_code)
) COMMENT='计费规则模板';

CREATE TABLE IF NOT EXISTS billing_merchant_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_no VARCHAR(64) NOT NULL COMMENT '商户号',
    biz_type INT NOT NULL COMMENT '业务类型',
    fee_mode INT NOT NULL COMMENT '计费模式',
    fee_type VARCHAR(8) NOT NULL DEFAULT '0' COMMENT '扣款方式',
    rate DECIMAL(10,6) DEFAULT 0 COMMENT '费率',
    fixed_fee DECIMAL(18,2) DEFAULT 0 COMMENT '固定手续费',
    min_fee DECIMAL(18,2) DEFAULT 0 COMMENT '最低手续费',
    max_fee DECIMAL(18,2) DEFAULT 0 COMMENT '最高手续费',
    currency VARCHAR(8) NOT NULL DEFAULT 'rmb' COMMENT '币种',
    status INT NOT NULL DEFAULT 1 COMMENT '1生效 0停用',
    effective_time VARCHAR(8) NOT NULL COMMENT '生效日期 yyyyMMdd',
    template_code VARCHAR(32) COMMENT '来源模板',
    create_user VARCHAR(64),
    update_user VARCHAR(64),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_merchant_biz_mode_eff (merchant_no, biz_type, fee_mode, effective_time)
) COMMENT='商户计费规则';

INSERT INTO billing_template (template_code, template_name, merchant_type, biz_type, fee_mode, fee_type, rate, fixed_fee, min_fee, max_fee, currency, status, remark, create_time, update_time)
VALUES
('DEFAULT_PAY', '默认付款费率', 'DEFAULT', 0, 1, '0', 0.006000, 0.00, 0.01, 500.00, 'rmb', 1, '入网默认付款费率', NOW(), NOW()),
('DEFAULT_REFUND', '默认退款费率', 'DEFAULT', 1, 1, '0', 0.003000, 0.00, 0.01, 100.00, 'rmb', 1, '入网默认退款费率', NOW(), NOW()),
('DEFAULT_RECEIPT', '默认收单费率', 'DEFAULT', 2, 4, '0', 0.000000, 0.00, 0.01, 500.00, 'rmb', 1, '入网默认收单梯度费率', NOW(), NOW())
ON DUPLICATE KEY UPDATE fee_mode = VALUES(fee_mode), remark = VALUES(remark), update_time = NOW();
