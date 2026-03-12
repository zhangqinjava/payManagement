package com.al.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class OrderQueryDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    private String bizType;
    private String orderNo;
    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp="\\d{8}",message = "开始日期格式不是yyyymmdd")
    private String startDate;
    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "\\d{8}",message = "结束日期格式不是yyyymmdd")
    private String endDate;
}
