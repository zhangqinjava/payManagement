package com.al.controller;

import com.al.bean.dto.MerchantDto;
import com.al.bean.dto.MerchantOnboardDto;
import com.al.service.MerchantOnboardService;
import com.al.service.MerchantService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/info")
public class MerchantController {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantOnboardService merchantOnboardService;

    @PostMapping("/save")
    public Result save(@RequestBody @Valid MerchantDto merchantDto) throws Exception{
        return Result.success(merchantService.save(merchantDto));
    }

    @PostMapping("/saveWithAccount")
    public Result saveWithAccount(@RequestBody @Valid MerchantOnboardDto dto) throws Exception {
        return Result.success(merchantOnboardService.onboard(dto));
    }
    @PostMapping("/update")
    public Result update(@RequestBody MerchantDto merchantDto) throws Exception{
        return Result.success(merchantService.update(merchantDto));
    }
    @GetMapping("/delete")
    public Result delete(@RequestParam String merchantNo) throws Exception{
        return Result.success(merchantService.delete(merchantNo));
    }
    @GetMapping("/query")
    public Result query(@RequestParam String merchantNo) throws Exception{
        return Result.success(merchantService.query(merchantNo));
    }

}
