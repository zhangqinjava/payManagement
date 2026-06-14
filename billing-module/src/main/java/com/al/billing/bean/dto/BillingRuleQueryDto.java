package com.al.billing.bean.dto;

import lombok.Data;

@Data
public class BillingRuleQueryDto {
    private String merchantNo;
    private String bizType;
    private Integer status;
}
