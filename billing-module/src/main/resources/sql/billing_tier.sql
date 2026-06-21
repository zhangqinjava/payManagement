USE billing;

CREATE TABLE IF NOT EXISTS billing_template_tier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(32) NOT NULL COMMENT '模板编码',
    tier_no INT NOT NULL COMMENT '档位序号，从1开始',
    min_amount DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '区间下限（含）',
    max_amount DECIMAL(18,2) DEFAULT NULL COMMENT '区间上限（不含），NULL表示无上限',
    rate DECIMAL(10,6) DEFAULT 0 COMMENT '该档费率',
    fixed_fee DECIMAL(18,2) DEFAULT 0 COMMENT '该档固定费',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark VARCHAR(128),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_template_tier (template_code, tier_no)
) COMMENT='计费模板梯度档位';

CREATE TABLE IF NOT EXISTS billing_merchant_tier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL COMMENT '商户计费规则ID',
    merchant_no VARCHAR(64) NOT NULL COMMENT '商户号',
    tier_no INT NOT NULL COMMENT '档位序号',
    min_amount DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '区间下限（含）',
    max_amount DECIMAL(18,2) DEFAULT NULL COMMENT '区间上限（不含）',
    rate DECIMAL(10,6) DEFAULT 0 COMMENT '该档费率',
    fixed_fee DECIMAL(18,2) DEFAULT 0 COMMENT '该档固定费',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_rule_tier (rule_id, tier_no),
    KEY idx_merchant_no (merchant_no)
) COMMENT='商户梯度计费档位';

-- 收单默认改为梯度计费
UPDATE billing_template
SET fee_mode = 4, rate = 0, remark = '入网默认收单梯度费率', update_time = NOW()
WHERE template_code = 'DEFAULT_RECEIPT';

INSERT INTO billing_template_tier (template_code, tier_no, min_amount, max_amount, rate, fixed_fee, status, remark, create_time, update_time)
VALUES
('DEFAULT_RECEIPT', 1, 0.00, 1000.00, 0.006000, 0.00, 1, '0-1000元档', NOW(), NOW()),
('DEFAULT_RECEIPT', 2, 1000.00, 10000.00, 0.005000, 0.00, 1, '1000-10000元档', NOW(), NOW()),
('DEFAULT_RECEIPT', 3, 10000.00, NULL, 0.004000, 0.00, 1, '10000元以上档', NOW(), NOW())
ON DUPLICATE KEY UPDATE rate = VALUES(rate), update_time = NOW();
