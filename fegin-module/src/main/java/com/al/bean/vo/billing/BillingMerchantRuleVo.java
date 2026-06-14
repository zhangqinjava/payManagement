package com.al.bean.vo.billing;

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
public class BillingMerchantRuleVo {
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
