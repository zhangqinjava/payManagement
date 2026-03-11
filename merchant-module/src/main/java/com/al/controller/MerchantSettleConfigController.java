package com.al.controller;

import com.al.bean.dto.MerchantSettleBindDTO;
import com.al.common.result.Result;
import com.al.service.MerchantSettleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settle")
public class MerchantSettleConfigController {
    @Autowired
    private MerchantSettleConfigService merchantSettleConfigService;
    @PostMapping("/insert")
    public Result insert(@RequestBody MerchantSettleBindDTO merchantSettleBindDTO) {
        return Result.success(merchantSettleConfigService.bindAccount(merchantSettleBindDTO));
    }
    @GetMapping("/query")
    public Result queryByMerchant(@RequestParam String merchantNo, @RequestParam String busiType ) {
        return Result.success(merchantSettleConfigService.queryByMerchant(merchantNo,busiType));
    }
    @GetMapping("/update")
    public Result update(@RequestParam String merchantNo, @RequestParam String busiType ) {
        return Result.success(merchantSettleConfigService.discard(merchantNo,busiType));
    }

}
