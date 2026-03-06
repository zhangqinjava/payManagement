package com.al.service.impl.caculate;

import com.al.bean.business.FeeModeEnum;
import com.al.bean.dto.CaculateDto;
import com.al.bean.vo.MerchantFeeVo;
import com.al.service.FeeCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component("3")
public class MixedFeeCaculator implements FeeCalculator {
    /**
     * 支持的计费模式
     */
    @Override
    public FeeModeEnum supportMode() {
        return FeeModeEnum.MIXED;
    }

    /**
     * @param dto
     * @param fee
     * @return
     */
    @Override
    public BigDecimal calculate(CaculateDto dto, MerchantFeeVo fee) {
        return fee.getFixedFee()
                .add(dto.getCaculateAmount().multiply(fee.getRate())).setScale(2, RoundingMode.HALF_UP);
    }
}
