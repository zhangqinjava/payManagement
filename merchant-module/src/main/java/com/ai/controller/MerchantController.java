package com.ai.controller;

import com.ai.bean.dto.MerchantDto;
import com.ai.service.MerchantService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/info")
public class MerchantController {
    @Autowired
    private MerchantService merchantService;
    @PostMapping("/save")
    public Result save(@RequestBody @Valid MerchantDto merchantDto) throws Exception{
        return Result.success(merchantService.save(merchantDto));
    }
    @PostMapping("/update")
    public Result update(@RequestBody MerchantDto merchantDto) throws Exception{
        return Result.success(merchantService.update(merchantDto));
    }
    @GetMapping("/delete")
    public Result delete(@RequestParam String merchantId) throws Exception{
        return Result.success(merchantService.delete(merchantId));
    }
    @GetMapping("/query")
    public Result query(@RequestParam String merchantId) throws Exception{
        return Result.success(merchantService.query(merchantId));
    }

}
