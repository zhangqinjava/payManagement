package com.ai.service.impl.caculate;

import com.ai.bean.business.FeeModeEnum;
import com.ai.bean.dto.CaculateDto;
import com.ai.bean.vo.MerchantFeeVo;
import com.ai.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component("2")
public class RateFeeCalculator implements FeeCalculator {
    @Override
    public FeeModeEnum supportMode() {
        return FeeModeEnum.RATE;
    }
    /**
     * 计算费率
     * @param dto
     * @param fee
     * @return
     */
    @Override
    public BigDecimal calculate(CaculateDto dto, MerchantFeeVo fee) {
        return dto.getCaculateAmount()
                .multiply(fee.getRate())
                .setScale(2, RoundingMode.HALF_UP);
    }
}
