package com.ai.service;

import com.ai.bean.business.FeeModeEnum;
import com.ai.bean.dto.CaculateDto;
import com.ai.bean.vo.MerchantFeeVo;

import java.math.BigDecimal;

public interface FeeCalculator {
    /**
     * 支持的计费模式
     */
    FeeModeEnum supportMode();
    /**
     *
     * @param dto
     * @param fee
     * @return
     */
    BigDecimal calculate(CaculateDto dto, MerchantFeeVo fee);

}
