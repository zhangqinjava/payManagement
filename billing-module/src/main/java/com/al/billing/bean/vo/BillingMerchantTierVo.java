package com.al.billing.bean.vo;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("billing_merchant_tier")
public class BillingMerchantTierVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private String merchantNo;
    private Integer tierNo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal rate;
    private BigDecimal fixedFee;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
