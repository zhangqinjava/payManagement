package com.ai.service.impl;

import com.ai.bean.dto.CaculateDto;
import com.ai.bean.vo.CaculateVo;
import com.ai.bean.vo.MerchantFeeVo;
import com.ai.config.FeeStrategyFactory;
import com.ai.service.CaculateService;
import com.ai.service.FeeCalculator;
import com.ai.service.MerchantRateService;
import com.al.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
public class CaculateServiceImpl implements CaculateService {
    @Autowired
    private MerchantRateService merchantRateService;
    @Autowired
    private FeeStrategyFactory factory;
    @Override
    public CaculateVo caculate(CaculateDto caculateDto) throws Exception {
        try {
            log.info("start caculate fee request param:{}", caculateDto);
            MerchantFeeVo merchantFeeVo = merchantRateService.selectOne(caculateDto);
            log.info("caculate fee rate configuration:{}", merchantFeeVo);
            if (Objects.isNull(merchantFeeVo)) {
                throw new BusinessException("未查询到相关的费率");
            }
            BigDecimal feeAmount = BigDecimal.ZERO;
            FeeCalculator strategy = factory.getStrategy(String.valueOf(merchantFeeVo.getFeeMode()));
            feeAmount = strategy.calculate(caculateDto, merchantFeeVo);
            CaculateVo build = CaculateVo.builder().rate(merchantFeeVo.getRate())
                    .feeAmount(feeAmount)
                    .amount(caculateDto.getCaculateAmount())
                    .busiType(merchantFeeVo.getBizType())
                    .feeMode(Integer.valueOf(merchantFeeVo.getFeeMode())).build();
            log.info("caculate fee end:{}", build);
            return build;
        }catch (Exception e) {
            log.error("caculate fee error:{}", e);
            throw e;
        }
    }
}
