package com.al.split.bean.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SplitReceiverDto {
    @NotBlank(message = "接收方商户号不能为空")
    private String receiverMerchantNo;
    @NotBlank(message = "接收方账户号不能为空")
    private String receiverAccountNo;
    @NotBlank(message = "接收方账户类型不能为空")
    private String receiverAccountType;
    @NotNull(message = "分账金额不能为空")
    @DecimalMin(value = "0.01", message = "分账金额必须大于0")
    private BigDecimal amount;
    private String splitType;
    private String remark;
}
