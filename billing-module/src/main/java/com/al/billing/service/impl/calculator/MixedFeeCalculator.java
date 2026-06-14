package com.al.billing.service.impl.calculator;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("3")
public class MixedFeeCalculator implements FeeCalculator {
    @Override
    public BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule) {
        BigDecimal fixed = rule.getFixedFee() == null ? BigDecimal.ZERO : rule.getFixedFee();
        BigDecimal ratePart = rule.getRate() == null ? BigDecimal.ZERO : amount.multiply(rule.getRate());
        return fixed.add(ratePart).setScale(2, RoundingMode.HALF_UP);
    }
}
