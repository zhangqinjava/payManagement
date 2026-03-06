package com.al.bean.dto.merchant;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
public class MerchantFeeDto {
    private Integer id;
    @NotBlank(message = "商户号不能为空")
    private  String merchantNo;
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "0|1|2",message = "不支持此业务类型")
    private  String  bizType;
    @NotBlank(message = "计费模式不能为空")
    @Pattern(regexp = "1|2|3",message = "不支持的计费类型")
    private String feeMode;
    private BigDecimal rate;
    private BigDecimal fixedFee;
    @NotBlank(message = "最低手续费不能为空")
    private BigDecimal minFee;
    @NotBlank(message = "最高手续费不能为空")
    private BigDecimal maxFee;
    @NotBlank(message = "币种不能为空")
    private String currency;
    private Integer status;
    @NotBlank(message = "生效时间不能为空")
    @Pattern(regexp="\\d{8}")
    private String effectiveTime;
    private String expireTime;
    private String caculateDate;
    private String createUser;
    private String updateUser;

}
