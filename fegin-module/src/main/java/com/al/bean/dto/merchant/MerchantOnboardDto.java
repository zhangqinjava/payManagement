package com.al.bean.dto.merchant;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class MerchantOnboardDto {
    @Valid
    @NotNull(message = "商户信息不能为空")
    private MerchantDto merchant;
    private String accountNo;
    private String accountType;
    private String channelCode;
    private String createUser;
    private String remark;
}
