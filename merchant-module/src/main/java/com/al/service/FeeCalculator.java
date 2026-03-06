package com.al.service;

import com.al.bean.business.FeeModeEnum;
import com.al.bean.dto.CaculateDto;
import com.al.bean.vo.MerchantFeeVo;

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
