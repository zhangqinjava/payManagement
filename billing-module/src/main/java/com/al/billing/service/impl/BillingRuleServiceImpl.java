package com.al.billing.service.impl;

import com.al.billing.bean.dto.BillingCalculateDto;
import com.al.billing.bean.dto.BillingOnboardOpenDto;
import com.al.billing.bean.dto.BillingRuleQueryDto;
import com.al.billing.bean.dto.BillingSplitCalculateDto;
import com.al.billing.bean.vo.BillingCalculateVo;
import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.bean.vo.BillingMerchantTierVo;
import com.al.billing.bean.vo.BillingOnboardOpenVo;
import com.al.billing.bean.vo.BillingSplitCalculateVo;
import com.al.billing.bean.vo.BillingTemplateTierVo;
import com.al.billing.bean.vo.BillingTemplateVo;
import com.al.billing.bean.vo.BillingTierDetailVo;
import com.al.billing.config.FeeStrategyFactory;
import com.al.billing.enums.FeeModeEnum;
import com.al.billing.mapper.BillingMerchantRuleMapper;
import com.al.billing.mapper.BillingMerchantTierMapper;
import com.al.billing.mapper.BillingTemplateMapper;
import com.al.billing.mapper.BillingTemplateTierMapper;
import com.al.billing.service.BillingRuleService;
import com.al.billing.service.FeeCalculator;
import com.al.billing.service.TierBillingService;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BillingRuleServiceImpl implements BillingRuleService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DEFAULT_MERCHANT_TYPE = "DEFAULT";
    private static final int STATUS_ACTIVE = 1;

    @Autowired
    private BillingTemplateMapper templateMapper;
    @Autowired
    private BillingTemplateTierMapper templateTierMapper;
    @Autowired
    private BillingMerchantRuleMapper ruleMapper;
    @Autowired
    private BillingMerchantTierMapper merchantTierMapper;
    @Autowired
    private FeeStrategyFactory feeStrategyFactory;
    @Autowired
    private TierBillingService tierBillingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingOnboardOpenVo openOnOnboard(BillingOnboardOpenDto dto) {
        log.info("open billing rules on onboard merchantNo={}", dto.getMerchantNo());
        String merchantType = StringUtils.isNotBlank(dto.getMerchantType())
                ? dto.getMerchantType() : DEFAULT_MERCHANT_TYPE;
        String effectiveTime = LocalDate.now().format(DATE_FMT);
        List<BillingTemplateVo> templates = loadTemplates(merchantType);
        if (CollectionUtils.isEmpty(templates)) {
            throw new BusinessException("未配置计费模板，无法自动开通");
        }

        List<BillingMerchantRuleVo> openedRules = new ArrayList<>();
        for (BillingTemplateVo template : templates) {
            BillingMerchantRuleVo exist = ruleMapper.selectOne(Wrappers.lambdaQuery(BillingMerchantRuleVo.class)
                    .eq(BillingMerchantRuleVo::getMerchantNo, dto.getMerchantNo())
                    .eq(BillingMerchantRuleVo::getBizType, template.getBizType())
                    .eq(BillingMerchantRuleVo::getFeeMode, template.getFeeMode())
                    .eq(BillingMerchantRuleVo::getEffectiveTime, effectiveTime));
            if (exist != null) {
                openedRules.add(exist);
                continue;
            }
            BillingMerchantRuleVo rule = fromTemplate(template, dto, effectiveTime);
            try {
                ruleMapper.insert(rule);
                copyTemplateTiersIfNeeded(rule, template.getTemplateCode());
                openedRules.add(rule);
            } catch (DuplicateKeyException e) {
                log.warn("billing rule duplicate merchantNo={}, bizType={}", dto.getMerchantNo(), template.getBizType());
            }
        }
        return BillingOnboardOpenVo.builder()
                .merchantNo(dto.getMerchantNo())
                .openedCount(openedRules.size())
                .rules(openedRules)
                .build();
    }

    @Override
    public List<BillingMerchantRuleVo> queryRules(BillingRuleQueryDto dto) {
        if (StringUtils.isBlank(dto.getMerchantNo())) {
            throw new BusinessException("商户号不能为空");
        }
        return ruleMapper.selectList(Wrappers.lambdaQuery(BillingMerchantRuleVo.class)
                .eq(BillingMerchantRuleVo::getMerchantNo, dto.getMerchantNo())
                .eq(StringUtils.isNotBlank(dto.getBizType()), BillingMerchantRuleVo::getBizType, Integer.valueOf(dto.getBizType()))
                .eq(dto.getStatus() != null, BillingMerchantRuleVo::getStatus, dto.getStatus())
                .orderByDesc(BillingMerchantRuleVo::getEffectiveTime));
    }

    @Override
    public BillingCalculateVo calculate(BillingCalculateDto dto) {
        String calculateDate = StringUtils.isNotBlank(dto.getCalculateDate())
                ? dto.getCalculateDate() : LocalDate.now().format(DATE_FMT);
        BillingMerchantRuleVo rule = resolveRule(dto.getMerchantNo(), dto.getBizType(), dto.getFeeMode(), calculateDate);
        FeeCalculator calculator = feeStrategyFactory.getStrategy(rule.getFeeMode());
        BigDecimal feeAmount = calculator.calculate(dto.getAmount(), rule);
        return BillingCalculateVo.builder()
                .merchantNo(dto.getMerchantNo())
                .bizType(rule.getBizType())
                .feeMode(rule.getFeeMode())
                .amount(dto.getAmount())
                .feeAmount(feeAmount)
                .rate(rule.getRate())
                .build();
    }

    @Override
    public BillingSplitCalculateVo calculateForSplit(BillingSplitCalculateDto dto) {
        String calculateDate = StringUtils.isNotBlank(dto.getCalculateDate())
                ? dto.getCalculateDate() : LocalDate.now().format(DATE_FMT);
        BillingMerchantRuleVo rule = resolveRule(dto.getMerchantNo(), dto.getBizType(), null, calculateDate);
        BigDecimal feeAmount;
        List<BillingTierDetailVo> tierDetails = null;
        if (FeeModeEnum.GRADIENT.getCode() == rule.getFeeMode()) {
            tierDetails = tierBillingService.calculateDetails(dto.getAmount(), rule);
            feeAmount = tierBillingService.calculate(dto.getAmount(), rule);
        } else {
            FeeCalculator calculator = feeStrategyFactory.getStrategy(rule.getFeeMode());
            feeAmount = calculator.calculate(dto.getAmount(), rule);
        }
        BigDecimal netAmount = dto.getAmount().subtract(feeAmount);
        if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("手续费超过交易金额");
        }
        BigDecimal effectiveRate = dto.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? feeAmount.divide(dto.getAmount(), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return BillingSplitCalculateVo.builder()
                .merchantNo(dto.getMerchantNo())
                .orderNo(dto.getOrderNo())
                .bizType(rule.getBizType())
                .feeMode(rule.getFeeMode())
                .ruleId(rule.getId())
                .amount(dto.getAmount())
                .feeAmount(feeAmount)
                .netAmount(netAmount)
                .effectiveRate(effectiveRate)
                .tierDetails(tierDetails)
                .build();
    }

    private BillingMerchantRuleVo resolveRule(String merchantNo, String bizType, String feeMode, String calculateDate) {
        if (StringUtils.isBlank(merchantNo)) {
            throw new BusinessException("商户号不能为空");
        }
        if (StringUtils.isBlank(bizType)) {
            throw new BusinessException("业务类型不能为空");
        }
        BillingMerchantRuleVo rule = ruleMapper.selectOne(Wrappers.lambdaQuery(BillingMerchantRuleVo.class)
                .eq(BillingMerchantRuleVo::getMerchantNo, merchantNo)
                .eq(BillingMerchantRuleVo::getBizType, Integer.valueOf(bizType))
                .eq(StringUtils.isNotBlank(feeMode), BillingMerchantRuleVo::getFeeMode, Integer.valueOf(feeMode))
                .eq(BillingMerchantRuleVo::getStatus, Integer.valueOf(BusiEnum.RATE_NOT_DISABLED.getCode()))
                .le(BillingMerchantRuleVo::getEffectiveTime, calculateDate)
                .orderByDesc(BillingMerchantRuleVo::getEffectiveTime)
                .orderByDesc(BillingMerchantRuleVo::getFeeMode)
                .last("LIMIT 1"));
        if (rule == null) {
            throw new BusinessException("未查询到商户计费规则");
        }
        return rule;
    }

    private void copyTemplateTiersIfNeeded(BillingMerchantRuleVo rule, String templateCode) {
        if (FeeModeEnum.GRADIENT.getCode() != rule.getFeeMode()) {
            return;
        }
        List<BillingTemplateTierVo> templateTiers = templateTierMapper.selectList(
                Wrappers.lambdaQuery(BillingTemplateTierVo.class)
                        .eq(BillingTemplateTierVo::getTemplateCode, templateCode)
                        .eq(BillingTemplateTierVo::getStatus, STATUS_ACTIVE)
                        .orderByAsc(BillingTemplateTierVo::getTierNo));
        if (CollectionUtils.isEmpty(templateTiers)) {
            throw new BusinessException("梯度模板未配置档位:" + templateCode);
        }
        LocalDateTime now = LocalDateTime.now();
        for (BillingTemplateTierVo templateTier : templateTiers) {
            BillingMerchantTierVo merchantTier = BillingMerchantTierVo.builder()
                    .ruleId(rule.getId())
                    .merchantNo(rule.getMerchantNo())
                    .tierNo(templateTier.getTierNo())
                    .minAmount(templateTier.getMinAmount())
                    .maxAmount(templateTier.getMaxAmount())
                    .rate(templateTier.getRate())
                    .fixedFee(templateTier.getFixedFee())
                    .status(STATUS_ACTIVE)
                    .createTime(now)
                    .updateTime(now)
                    .build();
            merchantTierMapper.insert(merchantTier);
        }
    }

    private List<BillingTemplateVo> loadTemplates(String merchantType) {
        List<BillingTemplateVo> specific = templateMapper.selectList(Wrappers.lambdaQuery(BillingTemplateVo.class)
                .eq(BillingTemplateVo::getMerchantType, merchantType)
                .eq(BillingTemplateVo::getStatus, STATUS_ACTIVE));
        if (CollectionUtils.isNotEmpty(specific)) {
            return specific;
        }
        return templateMapper.selectList(Wrappers.lambdaQuery(BillingTemplateVo.class)
                .eq(BillingTemplateVo::getMerchantType, DEFAULT_MERCHANT_TYPE)
                .eq(BillingTemplateVo::getStatus, STATUS_ACTIVE));
    }

    private BillingMerchantRuleVo fromTemplate(BillingTemplateVo template, BillingOnboardOpenDto dto, String effectiveTime) {
        LocalDateTime now = LocalDateTime.now();
        return BillingMerchantRuleVo.builder()
                .merchantNo(dto.getMerchantNo())
                .bizType(template.getBizType())
                .feeMode(template.getFeeMode())
                .feeType(template.getFeeType())
                .rate(template.getRate())
                .fixedFee(template.getFixedFee())
                .minFee(template.getMinFee())
                .maxFee(template.getMaxFee())
                .currency(template.getCurrency())
                .status(STATUS_ACTIVE)
                .effectiveTime(effectiveTime)
                .templateCode(template.getTemplateCode())
                .createUser(StringUtils.isNotBlank(dto.getCreateUser()) ? dto.getCreateUser() : "system")
                .updateUser(StringUtils.isNotBlank(dto.getCreateUser()) ? dto.getCreateUser() : "system")
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
