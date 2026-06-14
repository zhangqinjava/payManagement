CREATE TABLE IF NOT EXISTS settle_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settle_no VARCHAR(64) NOT NULL COMMENT '结算单号',
    flow_no VARCHAR(32) COMMENT '账务流水号',
    flow_dtl_no VARCHAR(32) COMMENT '账务明细号',
    biz_order_no VARCHAR(64) COMMENT '业务订单号',
    biz_type VARCHAR(8) COMMENT '业务类型',
    fun_code VARCHAR(8) COMMENT '功能码',
    amount DECIMAL(18,2) COMMENT '金额',
    fund_direction VARCHAR(2) COMMENT '资金方向 C/D',
    order_date VARCHAR(8) COMMENT '账务日期 yyyyMMdd',
    create_time DATETIME COMMENT '创建时间',
    KEY idx_settle_no (settle_no)
) COMMENT='结算明细（来源账务流水）';
