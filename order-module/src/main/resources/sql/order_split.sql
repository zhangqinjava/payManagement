CREATE TABLE IF NOT EXISTS order_split_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    split_no VARCHAR(64) NOT NULL COMMENT '分账单号',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    trade_no VARCHAR(64) COMMENT '交易号',
    merchant_no VARCHAR(64) NOT NULL COMMENT '商户号',
    total_amount DECIMAL(18,2) NOT NULL COMMENT '订单总金额',
    fee_amount DECIMAL(18,2) DEFAULT 0 COMMENT '手续费',
    net_amount DECIMAL(18,2) DEFAULT 0 COMMENT '商户净额',
    split_status INT NOT NULL DEFAULT 0 COMMENT '0待分账 1分账中 2成功 3部分失败 4失败',
    fail_reason VARCHAR(256) COMMENT '失败原因',
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_order_merchant (order_no, merchant_no),
    KEY idx_split_no (split_no)
) COMMENT='订单分账主表';

CREATE TABLE IF NOT EXISTS order_split_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    split_no VARCHAR(64) NOT NULL COMMENT '分账单号',
    detail_no VARCHAR(64) NOT NULL COMMENT '分账明细号',
    receiver_merchant_no VARCHAR(64) NOT NULL COMMENT '接收方商户号',
    receiver_account_no VARCHAR(64) NOT NULL COMMENT '接收方账户号',
    receiver_account_type VARCHAR(8) NOT NULL COMMENT '接收方账户类型',
    split_type VARCHAR(16) COMMENT 'FEE/NET/PARTNER',
    amount DECIMAL(18,2) NOT NULL COMMENT '分账金额',
    flow_no VARCHAR(64) COMMENT '账务流水号',
    status INT NOT NULL DEFAULT 0 COMMENT '明细状态',
    fail_reason VARCHAR(256) COMMENT '失败原因',
    create_time DATETIME,
    update_time DATETIME,
    KEY idx_split_no (split_no)
) COMMENT='订单分账明细';
