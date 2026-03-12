package com.al.settle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结算记录实体
 * 对应表：settle_record
 */
@Data
@TableName("settle_record")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettleRecordVo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 结算单号
     */
    private String settleNo;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 业务类型
     */
    private String busiType;

    /**
     * 结算周期类型：DAY-日结，WEEK-周结，MONTH-月结
     */
    private String settleCycle;

    /**
     * 结算周期开始日期
     */
    private LocalDate settleStartDate;

    /**
     * 结算周期结束日期
     */
    private LocalDate settleEndDate;

    /**
     * 结算日期（实际结算日期）
     */
    private LocalDate settleDate;

    /**
     * 订单总笔数
     */
    private Integer orderCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 手续费总额
     */
    private BigDecimal totalFee;

    /**
     * 结算净额（实际打款金额）
     */
    private BigDecimal netAmount;

    /**
     * 结算状态：0-待结算，1-结算中，2-结算成功，3-结算失败
     */
    private Integer settleStatus;

    /**
     * 打款状态：0-待打款，1-打款中，2-打款成功，3-打款失败
     */
    private Integer payStatus;

    /**
     * 打款时间
     */
    private LocalDateTime payTime;

    /**
     * 打款流水号
     */
    private String payFlowNo;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}