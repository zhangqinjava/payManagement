package com.al.billing.service;

import com.al.billing.bean.vo.BillingMerchantRuleVo;

import java.math.BigDecimal;

public interface FeeCalculator {
    BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule);
}
