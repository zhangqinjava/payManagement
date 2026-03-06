package com.al.account.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AccountQueryDto {
    @NotBlank(message = "账户号不能为空")
    private String accountNo;
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotBlank(message = "账户类型不能为空")
    private String acctType;
    @NotBlank(message = "通道号不能为空")
    private String channelNo;
    @NotBlank(message = "通道账户号")
    private String channelAccountNo;
    @NotBlank(message = "开始日期不能为空")
    @Pattern(regexp = "\\d{8}",message = "不符合格式yyyyMMdd")
    private String startDate;
    @NotBlank(message = "结束日期不能为空")
    @Pattern(regexp = "\\d{8}",message = "不符合格式yyyyMMdd")
    private String endDate;
    private String funcode;

}
