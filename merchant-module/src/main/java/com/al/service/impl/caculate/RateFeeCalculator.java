package com.al.service.impl.caculate;

import com.al.bean.business.FeeModeEnum;
import com.al.bean.dto.CaculateDto;
import com.al.bean.vo.MerchantFeeVo;
import com.al.service.FeeCalculator;
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
