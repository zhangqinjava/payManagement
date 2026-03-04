package com.ai.bean.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
public class CaculateDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantId;
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "0|1|2",message = "业务类型不能为空")
    private String bizType;
    @NotBlank(message = "计费模式不能为空")
    @Pattern(regexp = "1|2|3",message = "不支持的费率模式")
    private String feeMode;
    @NotBlank(message = "计算日期")
    private String caculateDate;
    @NotBlank(message = "交易金额字段不能为空")
    @DecimalMin(value = "0.01", message = "交易金额不能小于 0.01")
    private BigDecimal caculateAmount;
}
