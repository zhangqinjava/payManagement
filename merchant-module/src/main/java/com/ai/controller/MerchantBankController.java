package com.ai.controller;

import com.ai.bean.dto.MerchantBankDto;
import com.ai.service.MerchantBankService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/bank")
public class MerchantBankController {
    @Autowired
    private MerchantBankService merchantBankService;
    @PostMapping("/query")
    public Result query(@RequestBody  MerchantBankDto merchantBankDto) {
        return Result.success(merchantBankService.query(merchantBankDto));
    }
    @PostMapping("/save")
    public Result save(@RequestBody @Valid MerchantBankDto merchantBankDto) {
        return Result.success(merchantBankService.save(merchantBankDto));
    }
    @PostMapping("/update")
    public Result update(@RequestBody MerchantBankDto merchantBankDto) {
        return Result.success(merchantBankService.update(merchantBankDto));
    }
    @GetMapping("/delete")
    public Result delete(MerchantBankDto merchantBankDto) {
        return Result.success(merchantBankService.delete(merchantBankDto));
    }


}
