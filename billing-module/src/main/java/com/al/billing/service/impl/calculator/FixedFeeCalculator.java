package com.al.billing.service.impl.calculator;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("2")
public class FixedFeeCalculator implements FeeCalculator {
    @Override
    public BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule) {
        return rule.getFixedFee() == null ? BigDecimal.ZERO : rule.getFixedFee().setScale(2, RoundingMode.HALF_UP);
    }
}
