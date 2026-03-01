package com.ai.bean.dto;

import com.alibaba.nacos.shaded.org.checkerframework.checker.regex.qual.Regex;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class MerchantBankDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantId;
    @NotBlank(message = "卡类型不能为空")
    @Pattern(regexp = "1|2",message = "不支持的卡类型")
    private String cardType;
    @NotBlank(message = "银行编码不能为空")
    private String bankCode;
    @NotBlank(message = "银行名称不能为空")
    private String bankName;
    @NotBlank(message = "卡号不能为空")
    private String cardNo;
    @NotBlank(message = "持卡人姓名不能为空")
    private String cardName;
    @NotBlank(message = "证件号不能为空")
    private String idCard;
    @NotBlank(message = "证件类型不能为空")
    private String idCardType;
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @Pattern(regexp = "0|1",message = "是否默认只支持0-非默认 1-默认")
    private Integer isDefault;
    @Pattern(regexp = "0|1|2",message = "不支持其他状态")
    private String status;
    private Integer id;
    private String remark;
}
