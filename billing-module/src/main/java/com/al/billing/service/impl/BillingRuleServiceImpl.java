package com.al.billing.service.impl;

import com.al.billing.bean.dto.BillingCalculateDto;
import com.al.billing.bean.dto.BillingOnboardOpenDto;
import com.al.billing.bean.dto.BillingRuleQueryDto;
import com.al.billing.bean.vo.BillingCalculateVo;
import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.bean.vo.BillingOnboardOpenVo;
import com.al.billing.bean.vo.BillingTemplateVo;
import com.al.billing.config.FeeStrategyFactory;
import com.al.billing.mapper.BillingMerchantRuleMapper;
import com.al.billing.mapper.BillingTemplateMapper;
import com.al.billing.service.BillingRuleService;
import com.al.billing.service.FeeCalculator;
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
    private BillingMerchantRuleMapper ruleMapper;
    @Autowired
    private FeeStrategyFactory feeStrategyFactory;

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
        Integer feeMode = StringUtils.isNotBlank(dto.getFeeMode()) ? Integer.valueOf(dto.getFeeMode()) : 1;
        BillingMerchantRuleVo rule = ruleMapper.selectOne(Wrappers.lambdaQuery(BillingMerchantRuleVo.class)
                .eq(BillingMerchantRuleVo::getMerchantNo, dto.getMerchantNo())
                .eq(BillingMerchantRuleVo::getBizType, Integer.valueOf(dto.getBizType()))
                .eq(BillingMerchantRuleVo::getFeeMode, feeMode)
                .eq(BillingMerchantRuleVo::getStatus, Integer.valueOf(BusiEnum.RATE_NOT_DISABLED.getCode()))
                .le(BillingMerchantRuleVo::getEffectiveTime, calculateDate)
                .orderByDesc(BillingMerchantRuleVo::getEffectiveTime)
                .last("LIMIT 1"));
        if (rule == null) {
            throw new BusinessException("未查询到商户计费规则");
        }
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
