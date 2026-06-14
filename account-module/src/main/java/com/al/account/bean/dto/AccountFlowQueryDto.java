package com.al.account.bean.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class AccountFlowQueryDto {
    @NotBlank(message = "流水号不能为空")
    @Length(max = 32, message = "流水号最长为32位")
    private String flowNo;
}
