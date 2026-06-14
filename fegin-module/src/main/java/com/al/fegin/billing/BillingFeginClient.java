package com.al.fegin.billing;

import com.al.bean.dto.billing.BillingCalculateDto;
import com.al.bean.dto.billing.BillingOnboardOpenDto;
import com.al.bean.dto.billing.BillingRuleQueryDto;
import com.al.bean.vo.billing.BillingCalculateVo;
import com.al.bean.vo.billing.BillingMerchantRuleVo;
import com.al.bean.vo.billing.BillingOnboardOpenVo;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "billing-module", path = "/billing")
public interface BillingFeginClient {

    @PostMapping("/rule/onboard/open")
    Result<BillingOnboardOpenVo> openOnOnboard(@RequestBody BillingOnboardOpenDto dto);

    @PostMapping("/rule/query")
    Result<List<BillingMerchantRuleVo>> queryRules(@RequestBody BillingRuleQueryDto dto);

    @PostMapping("/rule/calculate")
    Result<BillingCalculateVo> calculate(@RequestBody BillingCalculateDto dto);
}
