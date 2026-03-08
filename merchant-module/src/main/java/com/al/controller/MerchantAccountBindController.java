package com.al.controller;
import com.al.bean.vo.MerchantAccountBindVo;
import com.al.service.MerchantAccountBindService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class MerchantAccountBindController {
    @Autowired
    private MerchantAccountBindService service;

    @PostMapping("/bind")
    public Result bind(@RequestBody MerchantAccountBindVo bind) throws Exception {
        service.bindAccount(bind);
        return Result.success("绑定成功");
    }

    @PostMapping("/unbind")
    public Result unbind(@RequestParam String merchantNo, @RequestParam String accountNo,
                         @RequestParam String updateUser) {
        service.unbindAccount(merchantNo, accountNo, updateUser);
        return Result.success("解绑成功");
    }

    @GetMapping("/listByMerchant")
    public Result listByMerchant(@RequestParam String merchantNo, @RequestParam String acctType) {
        List<MerchantAccountBindVo> list = service.getAccountsByMerchant(merchantNo,acctType);
        return Result.success(list);
    }

    @GetMapping("/listByAccount")
    public Result listByAccount(@RequestParam String accountNo) {
        List<MerchantAccountBindVo> list = service.getMerchantsByAccount(accountNo);
        return Result.success(list);
    }
}
