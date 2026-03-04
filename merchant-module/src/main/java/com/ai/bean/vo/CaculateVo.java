package com.ai.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaculateVo {
    private BigDecimal feeAmount;
    private BigDecimal amount;
    private BigDecimal rate;
    private String merchantId;
    private Integer feeMode;
    private Integer busiType;

}
