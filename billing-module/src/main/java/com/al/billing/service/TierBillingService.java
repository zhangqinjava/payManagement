package com.al.billing.service;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.bean.vo.BillingTierDetailVo;

import java.math.BigDecimal;
import java.util.List;

public interface TierBillingService {

    BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule);

    List<BillingTierDetailVo> calculateDetails(BigDecimal amount, BillingMerchantRuleVo rule);
}
