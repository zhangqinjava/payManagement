package com.al.controller;

import com.al.bean.dto.MerchantFeeDto;
import com.al.service.MerchantRateService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/fee")
public class MerchantFeeController {
    @Autowired
    private MerchantRateService merchantRateService;
    @GetMapping("/query")
    public Result<Object> query(@RequestParam MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.query(merchantFeeDto));
    }
    @GetMapping("/save")
    public Result<Object> save(@Valid  @RequestParam MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.save(merchantFeeDto));
    }
    @GetMapping("/update")
    public Result<Object> update(@RequestParam MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.update(merchantFeeDto));
    }
}
