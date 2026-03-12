package com.al.bean.dto.merchant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantSettleBindDTO {

    private String merchantNo;

    private String accountNo;

    private String accountType;

    private String busiType;

    private String settleCycle;

    private Integer settleDelay;

}
