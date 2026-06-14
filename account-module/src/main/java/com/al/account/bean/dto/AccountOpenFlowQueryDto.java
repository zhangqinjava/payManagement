package com.al.account.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AccountOpenFlowQueryDto {
    private String merchantNo;
    private String accountNo;
    @Pattern(regexp = "\\d{8}", message = "不符合格式yyyyMMdd")
    private String startDate;
    @Pattern(regexp = "\\d{8}", message = "不符合格式yyyyMMdd")
    private String endDate;
}
