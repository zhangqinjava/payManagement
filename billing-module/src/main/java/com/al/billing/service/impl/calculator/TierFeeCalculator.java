package com.al.billing.service.impl.calculator;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.service.FeeCalculator;
import com.al.billing.service.TierBillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("4")
public class TierFeeCalculator implements FeeCalculator {

    @Autowired
    private TierBillingService tierBillingService;

    @Override
    public BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule) {
        return tierBillingService.calculate(amount, rule);
    }
}
