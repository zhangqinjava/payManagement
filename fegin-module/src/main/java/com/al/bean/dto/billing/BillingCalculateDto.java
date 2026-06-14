package com.al.bean.dto.billing;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingCalculateDto {
    private String merchantNo;
    private String bizType;
    private String feeMode;
    private String calculateDate;
    private BigDecimal amount;
}
