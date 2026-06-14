package com.al.bean.dto.account;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SettlePayoutDto {
    @NotBlank(message = "流水号不能为空")
    private String flowNo;
    @NotBlank(message = "账户号不能为空")
    private String accountNo;
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotBlank(message = "账户类型不能为空")
    private String accountType;
    @NotBlank(message = "渠道不能为空")
    @Pattern(regexp = "1|2|3|4", message = "不支持的渠道类型")
    private String channelCode;
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
    @NotBlank(message = "出款金额不能为空")
    @Pattern(regexp = "\\d+", message = "金额只能是数字")
    private String amount;
    private String remark;
}
