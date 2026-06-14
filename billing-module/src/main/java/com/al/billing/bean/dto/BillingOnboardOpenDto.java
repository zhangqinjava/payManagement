package com.al.billing.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BillingOnboardOpenDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    private String merchantType;
    private String createUser;
}
