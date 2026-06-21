package com.al.billing.bean.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BillingSplitCalculateDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    private String orderNo;
    private String calculateDate;
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额不能小于0.01")
    private BigDecimal amount;
}
