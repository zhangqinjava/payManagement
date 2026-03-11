package com.al.controller;

import com.al.bean.dto.MerchantChannelConfigDto;
import com.al.common.result.Result;
import com.al.service.MerchantChannelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/channel")
public class MerchantChannelConfigController {
    @Autowired
    private MerchantChannelConfigService merchantChannelConfigService;
    @GetMapping("/list")
    public Result list(@RequestParam  MerchantChannelConfigDto merchantChannelConfigDto) throws Exception {
        return Result.success(merchantChannelConfigService.list(merchantChannelConfigDto));
    }
    @PostMapping("/save")
    public Result save(@RequestBody @Valid MerchantChannelConfigDto merchantChannelConfigDto) throws Exception {
        return Result.success(merchantChannelConfigService.save(merchantChannelConfigDto));
    }
    @PostMapping("/update")
    public Result update(@RequestBody  MerchantChannelConfigDto merchantChannelConfigDto) throws Exception {
        return Result.success(merchantChannelConfigService.update(merchantChannelConfigDto));
    }
    @GetMapping("/delete")
    public Result delete(@RequestParam Integer id) throws Exception {
        return Result.success(merchantChannelConfigService.delete(id));
    }
}
