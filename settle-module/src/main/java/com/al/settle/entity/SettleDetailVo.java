package com.al.settle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("settle_detail")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettleDetailVo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settleNo;
    private String flowNo;
    private String flowDtlNo;
    private String bizOrderNo;
    private String bizType;
    private String funCode;
    private BigDecimal amount;
    private String fundDirection;
    private String orderDate;
    private LocalDateTime createTime;
}
