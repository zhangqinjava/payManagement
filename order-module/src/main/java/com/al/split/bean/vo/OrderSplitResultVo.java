package com.al.split.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSplitResultVo {
    private String splitNo;
    private String orderNo;
    private String tradeNo;
    private String merchantNo;
    private BigDecimal totalAmount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private Integer splitStatus;
    private List<OrderSplitDetailVo> details;
}
