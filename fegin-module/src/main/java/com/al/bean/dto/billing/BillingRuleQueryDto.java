package com.al.bean.dto.billing;

import lombok.Data;

@Data
public class BillingRuleQueryDto {
    private String merchantNo;
    private String bizType;
    private Integer status;
}
