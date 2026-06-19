CREATE DATABASE IF NOT EXISTS reconcile DEFAULT CHARACTER SET utf8mb4;

USE reconcile;

CREATE TABLE IF NOT EXISTS reconcile_script (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    script_code VARCHAR(64) NOT NULL COMMENT '脚本编码',
    script_name VARCHAR(128) NOT NULL COMMENT '脚本名称',
    script_type VARCHAR(16) NOT NULL COMMENT 'PARSE/COMPARE/ALL',
    channel_code VARCHAR(32) DEFAULT 'DEFAULT' COMMENT '渠道编码',
    script_content MEDIUMTEXT NOT NULL COMMENT 'Groovy脚本内容',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号，变更后递增',
    status INT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark VARCHAR(256),
    create_user VARCHAR(64),
    update_user VARCHAR(64),
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_script_code (script_code)
) COMMENT='对账Groovy脚本';

CREATE TABLE IF NOT EXISTS reconcile_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(64) NOT NULL COMMENT '对账任务号',
    reconcile_date VARCHAR(8) NOT NULL COMMENT '对账日期 yyyyMMdd',
    channel_code VARCHAR(32) NOT NULL COMMENT '渠道编码',
    parse_script_code VARCHAR(64) COMMENT '解析脚本',
    compare_script_code VARCHAR(64) COMMENT '比对脚本',
    merchant_no VARCHAR(64) COMMENT '商户号',
    status INT NOT NULL DEFAULT 0 COMMENT '0处理中 1成功 2失败',
    local_count INT DEFAULT 0 COMMENT '本地笔数',
    remote_count INT DEFAULT 0 COMMENT '渠道笔数',
    diff_count INT DEFAULT 0 COMMENT '差异笔数',
    error_msg VARCHAR(512),
    create_time DATETIME,
    finish_time DATETIME,
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_reconcile_date (reconcile_date)
) COMMENT='对账任务';

CREATE TABLE IF NOT EXISTS reconcile_diff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(64) NOT NULL COMMENT '任务号',
    diff_type VARCHAR(32) NOT NULL COMMENT '差异类型',
    biz_key VARCHAR(128) COMMENT '业务主键，如订单号',
    local_amount DECIMAL(18,2) COMMENT '本地金额',
    remote_amount DECIMAL(18,2) COMMENT '渠道金额',
    diff_amount DECIMAL(18,2) COMMENT '差异金额',
    diff_detail VARCHAR(512) COMMENT '差异说明',
    status INT NOT NULL DEFAULT 0 COMMENT '0待处理 1已核销',
    create_time DATETIME,
    KEY idx_task_no (task_no),
    KEY idx_biz_key (biz_key)
) COMMENT='对账差异明细';

INSERT INTO reconcile_script (script_code, script_name, script_type, channel_code, script_content, version, status, remark, create_user, update_user, create_time, update_time)
VALUES
('DEFAULT_CSV_PARSE', '默认CSV解析', 'PARSE', 'DEFAULT',
'def parse(ctx, rawContent) {
    def rows = []
    if (!rawContent) {
        return rows
    }
    rawContent.eachLine { line ->
        def text = line?.trim()
        if (!text || text.startsWith("#")) {
            return
        }
        def parts = text.split(",")
        if (parts.length < 3) {
            return
        }
        rows << [
            orderNo: parts[0].trim(),
            amount: new BigDecimal(parts[1].trim()),
            tradeDate: parts[2].trim(),
            channelCode: ctx.channelCode
        ]
    }
    return rows
}',
1, 1, '解析渠道CSV: orderNo,amount,tradeDate', 'system', 'system', NOW(), NOW()),

('DEFAULT_ORDER_COMPARE', '默认订单比对', 'COMPARE', 'DEFAULT',
'def compare(ctx, localRows, remoteRows) {
    def diffs = []
    def remoteMap = [:]
    remoteRows?.each { row ->
        if (row?.orderNo) {
            remoteMap[row.orderNo] = row
        }
    }
    def matchedRemote = [] as Set
    localRows?.each { local ->
        def remote = remoteMap[local.orderNo]
        if (!remote) {
            diffs << [
                diffType: "LOCAL_ONLY",
                bizKey: local.orderNo,
                localAmount: local.amount,
                remoteAmount: null,
                diffAmount: local.amount,
                diffDetail: "本地有渠道无"
            ]
            return
        }
        matchedRemote << local.orderNo
        def localAmt = local.amount ?: BigDecimal.ZERO
        def remoteAmt = remote.amount ?: BigDecimal.ZERO
        if (localAmt.compareTo(remoteAmt) != 0) {
            diffs << [
                diffType: "AMOUNT_DIFF",
                bizKey: local.orderNo,
                localAmount: localAmt,
                remoteAmount: remoteAmt,
                diffAmount: localAmt.subtract(remoteAmt),
                diffDetail: "金额不一致"
            ]
        }
    }
    remoteRows?.each { remote ->
        if (remote?.orderNo && !matchedRemote.contains(remote.orderNo)) {
            diffs << [
                diffType: "REMOTE_ONLY",
                bizKey: remote.orderNo,
                localAmount: null,
                remoteAmount: remote.amount,
                diffAmount: remote.amount,
                diffDetail: "渠道有本地无"
            ]
        }
    }
    return diffs
}',
1, 1, '按订单号+金额比对', 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();
