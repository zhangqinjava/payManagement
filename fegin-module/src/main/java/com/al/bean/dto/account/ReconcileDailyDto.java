package com.al.bean.dto.account;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class ReconcileDailyDto {
    @NotBlank(message = "对账日期不能为空")
    @Pattern(regexp = "\\d{8}", message = "不符合格式yyyyMMdd")
    private String reconcileDate;
    private String merchantNo;
    private String accountNo;
}
