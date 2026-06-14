package com.al.billing.controller;

import com.al.billing.bean.dto.BillingCalculateDto;
import com.al.billing.bean.dto.BillingOnboardOpenDto;
import com.al.billing.bean.dto.BillingRuleQueryDto;
import com.al.billing.service.BillingRuleService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/rule")
public class BillingRuleController {

    @Autowired
    private BillingRuleService billingRuleService;

    @PostMapping("/onboard/open")
    public Result openOnOnboard(@Valid @RequestBody BillingOnboardOpenDto dto) {
        return Result.success(billingRuleService.openOnOnboard(dto));
    }

    @PostMapping("/query")
    public Result query(@RequestBody BillingRuleQueryDto dto) {
        return Result.success(billingRuleService.queryRules(dto));
    }

    @PostMapping("/calculate")
    public Result calculate(@Valid @RequestBody BillingCalculateDto dto) {
        return Result.success(billingRuleService.calculate(dto));
    }
}
