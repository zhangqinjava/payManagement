package com.al.bean.dto.account;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SettleClearDto {
    @NotBlank(message = "流水号不能为空")
    private String flowNo;
    @NotBlank(message = "转出账户号不能为空")
    private String outAccountNo;
    @NotBlank(message = "转出商户号不能为空")
    private String outMerchantNo;
    @NotBlank(message = "转出账户类型不能为空")
    private String outAccountType;
    @NotBlank(message = "转入账户号不能为空")
    private String inAccountNo;
    @NotBlank(message = "转入商户号不能为空")
    private String inMerchantNo;
    @NotBlank(message = "转入账户类型不能为空")
    private String inAccountType;
    @NotBlank(message = "金额不能为空")
    @Pattern(regexp = "\\d+", message = "金额只能是数字")
    private String amount;
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "0|1", message = "不支持的业务类型")
    private String bizType;
    @NotBlank(message = "业务订单号不能为空")
    private String bizOrderNo;
    @NotBlank(message = "业务订单日期不能为空")
    @Pattern(regexp = "\\d{8}$", message = "必须是8位的数字")
    private String bizOrderDate;
    @NotBlank(message = "业务订单时间不能为空")
    @Pattern(regexp = "\\d{6}$", message = "必须是6位的数字")
    private String bizOrderTime;
    private String channelCode;
    private String remark;
}
