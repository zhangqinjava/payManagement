package com.al.billing.service;

import com.al.billing.bean.dto.BillingCalculateDto;
import com.al.billing.bean.dto.BillingOnboardOpenDto;
import com.al.billing.bean.dto.BillingRuleQueryDto;
import com.al.billing.bean.dto.BillingSplitCalculateDto;
import com.al.billing.bean.vo.BillingCalculateVo;
import com.al.billing.bean.vo.BillingMerchantRuleVo;
import com.al.billing.bean.vo.BillingOnboardOpenVo;
import com.al.billing.bean.vo.BillingSplitCalculateVo;
import com.al.billing.bean.vo.BillingTierDetailVo;

import java.util.List;

public interface BillingRuleService {
    BillingOnboardOpenVo openOnOnboard(BillingOnboardOpenDto dto);

    List<BillingMerchantRuleVo> queryRules(BillingRuleQueryDto dto);

    BillingCalculateVo calculate(BillingCalculateDto dto);

    BillingSplitCalculateVo calculateForSplit(BillingSplitCalculateDto dto);
}
