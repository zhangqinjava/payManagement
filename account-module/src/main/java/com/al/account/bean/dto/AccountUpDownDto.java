package com.al.account.bean.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AccountUpDownDto {
    @NotBlank(message = "流水号不能为空")
    @Length(max = 32,message = "流水号最长为32位")
    private String flowNo;
    @NotBlank(message = "账户号不能为空")
    private String accountNo;
    @NotBlank(message = "商户号不能为空")
    private String storeId;
    @NotBlank(message = "账户类型不能为空")
    private String accountType;
    @NotBlank(message = "渠道不能为空")
    @Pattern(regexp = "1|2|3|4",message = "不支持的渠道类型")
    private String channelCode;
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "0|1",message = "不支持的业务类型")
    private String bizType;
    @NotBlank(message = "业务订单号不能为空")
    private String bizOrderNo;
    @NotBlank(message = "业务订单日期不能为空")
    @Pattern(regexp = "\\d{8}$",message = "必须是8位的数字")
    private String bizOrderDate;
    @NotBlank(message = "业务订单时间不能为空")
    @Pattern(regexp = "\\d{6}$",message = "必须是6位的数字")
    private String bizOrderTime;
    @NotBlank(message = "订单金额不能为空")
    @Pattern(regexp = "\\d+", message = "金额只能是数字")
    private String amount;
    @NotBlank(message = "功能码不能为空")
    @Pattern(regexp = "0601|0602|0603")
    private String funCode;
    private String remark;


}
