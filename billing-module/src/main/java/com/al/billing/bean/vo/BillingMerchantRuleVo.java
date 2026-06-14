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
@TableName("billing_merchant_rule")
public class BillingMerchantRuleVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String merchantNo;
    private Integer bizType;
    private Integer feeMode;
    private String feeType;
    private BigDecimal rate;
    private BigDecimal fixedFee;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private String currency;
    private Integer status;
    private String effectiveTime;
    private String templateCode;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
