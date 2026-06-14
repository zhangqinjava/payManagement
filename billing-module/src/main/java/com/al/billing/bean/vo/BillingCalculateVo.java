package com.al.billing.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingCalculateVo {
    private String merchantNo;
    private Integer bizType;
    private Integer feeMode;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal rate;
}
