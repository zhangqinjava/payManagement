package com.al.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class MerchantDto {
    private Integer id;
    @NotBlank(message = "商户名称不能为空")
    private String merchantName;
    private String merchantNo;
    @NotBlank(message = "商户类型不能为空")
    private String merchantType;
    private Integer status;
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp="^1[3-9]\\d{9}$",message = "手机号不正确")
    private String contactPhone;
}
