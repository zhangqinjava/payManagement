package com.al.controller;

import com.al.bean.dto.MerchantFeeDto;
import com.al.service.MerchantRateService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/query")
    public Result<Object> queryByBody(@RequestBody MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.query(merchantFeeDto));
    }
    @GetMapping("/save")
    public Result<Object> save(@Valid  @ModelAttribute MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.save(merchantFeeDto));
    }
    @GetMapping("/update")
    public Result<Object> update(@RequestParam MerchantFeeDto merchantFeeDto) throws Exception {
        return Result.success(merchantRateService.update(merchantFeeDto));
    }
}
