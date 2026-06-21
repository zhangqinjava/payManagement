package com.al.bean.vo.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingTierDetailVo {
    private Integer tierNo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal tierAmount;
    private BigDecimal rate;
    private BigDecimal tierFee;
}
