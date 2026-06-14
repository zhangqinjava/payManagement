package com.al.billing.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingOnboardOpenVo {
    private String merchantNo;
    private Integer openedCount;
    private List<BillingMerchantRuleVo> rules;
}
