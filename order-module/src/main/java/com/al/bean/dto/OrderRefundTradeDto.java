package com.al.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRefundTradeDto {
    private String orderNo;
    private String merchantNo;
    private BigDecimal refundAmount;
    private String reason;
}
