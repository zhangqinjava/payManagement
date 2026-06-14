package com.al.billing.service.impl.calculator;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("1")
public class RateFeeCalculator implements FeeCalculator {
    @Override
    public BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule) {
        BigDecimal fee = amount.multiply(rule.getRate()).setScale(2, RoundingMode.HALF_UP);
        return clamp(fee, rule.getMinFee(), rule.getMaxFee());
    }

    private BigDecimal clamp(BigDecimal fee, BigDecimal min, BigDecimal max) {
        if (min != null && fee.compareTo(min) < 0) {
            fee = min;
        }
        if (max != null && max.compareTo(BigDecimal.ZERO) > 0 && fee.compareTo(max) > 0) {
            fee = max;
        }
        return fee;
    }
}
