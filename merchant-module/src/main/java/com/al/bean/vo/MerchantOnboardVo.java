package com.al.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOnboardVo {
    private MerchantVo merchant;
    private MerchantAccountBindVo accountBind;
}
