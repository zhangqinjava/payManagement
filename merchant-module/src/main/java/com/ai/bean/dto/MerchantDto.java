package com.ai.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MerchantDto {
    private Integer id;
    @NotBlank(message = "商户名称不能为空")
    private String merchantName;
    private String merchantId;
    @NotBlank(message = "商户类型不能为空")
    private String merchantType;
    private Integer status;
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
}
