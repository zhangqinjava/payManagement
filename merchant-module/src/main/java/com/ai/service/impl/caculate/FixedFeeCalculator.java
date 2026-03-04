package com.ai.service.impl.caculate;

import com.ai.bean.business.FeeModeEnum;
import com.ai.bean.dto.CaculateDto;
import com.ai.bean.vo.MerchantFeeVo;
import com.ai.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component("1")
public class FixedFeeCalculator implements FeeCalculator {
    /**
     * 支持的计费模式
     */
    @Override
    public FeeModeEnum supportMode() {
        return FeeModeEnum.FIXED;
    }

    /**
     * @param dto
     * @param fee
     * @return
     */
    @Override
    public BigDecimal calculate(CaculateDto dto, MerchantFeeVo fee) {
        return fee.getFixedFee()
                .setScale(2, RoundingMode.HALF_UP);
    }
}
