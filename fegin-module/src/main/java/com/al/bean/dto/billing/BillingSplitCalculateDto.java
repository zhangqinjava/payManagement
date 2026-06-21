package com.al.bean.dto.billing;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingSplitCalculateDto {
    private String merchantNo;
    private String bizType;
    private String orderNo;
    private String calculateDate;
    private BigDecimal amount;
}
