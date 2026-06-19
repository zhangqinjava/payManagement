package com.al.billing.service.impl;

import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.bean.vo.BillingMerchantTierVo;
import com.al.billing.bean.vo.BillingTierDetailVo;
import com.al.billing.mapper.BillingMerchantTierMapper;
import com.al.billing.service.TierBillingService;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TierBillingServiceImpl implements TierBillingService {

    private static final int STATUS_ACTIVE = 1;

    @Autowired
    private BillingMerchantTierMapper tierMapper;

    @Override
    public BigDecimal calculate(BigDecimal amount, BillingMerchantRuleVo rule) {
        List<BillingTierDetailVo> details = calculateDetails(amount, rule);
        BigDecimal totalFee = details.stream()
                .map(BillingTierDetailVo::getTierFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return clamp(totalFee, rule.getMinFee(), rule.getMaxFee());
    }

    @Override
    public List<BillingTierDetailVo> calculateDetails(BigDecimal amount, BillingMerchantRuleVo rule) {
        List<BillingMerchantTierVo> tiers = tierMapper.selectList(Wrappers.lambdaQuery(BillingMerchantTierVo.class)
                .eq(BillingMerchantTierVo::getRuleId, rule.getId())
                .eq(BillingMerchantTierVo::getStatus, STATUS_ACTIVE)
                .orderByAsc(BillingMerchantTierVo::getTierNo));
        if (CollectionUtils.isEmpty(tiers)) {
            throw new BusinessException("梯度计费规则未配置档位");
        }

        BigDecimal remaining = amount;
        List<BillingTierDetailVo> details = new ArrayList<>();
        for (BillingMerchantTierVo tier : tiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierMin = tier.getMinAmount() == null ? BigDecimal.ZERO : tier.getMinAmount();
            BigDecimal bandCapacity = tier.getMaxAmount() == null
                    ? remaining
                    : tier.getMaxAmount().subtract(tierMin);
            if (bandCapacity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal tierAmount = remaining.min(bandCapacity);
            BigDecimal rate = tier.getRate() == null ? BigDecimal.ZERO : tier.getRate();
            BigDecimal fixedFee = tier.getFixedFee() == null ? BigDecimal.ZERO : tier.getFixedFee();
            BigDecimal tierFee = tierAmount.multiply(rate).add(fixedFee).setScale(2, RoundingMode.HALF_UP);
            details.add(BillingTierDetailVo.builder()
                    .tierNo(tier.getTierNo())
                    .minAmount(tierMin)
                    .maxAmount(tier.getMaxAmount())
                    .tierAmount(tierAmount)
                    .rate(rate)
                    .tierFee(tierFee)
                    .build());
            remaining = remaining.subtract(tierAmount);
        }
        return details;
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
