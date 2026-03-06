package com.al.controller;
import com.al.bean.vo.MerchantAccountBindVo;
import com.al.service.MerchantAccountBindService;
import com.al.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/account")
public class MerchantAccountBindController {

    private final MerchantAccountBindService service;

    public MerchantAccountBindController(MerchantAccountBindService service) {
        this.service = service;
    }

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
    public Result listByMerchant(@RequestParam String merchantNo) {
        List<MerchantAccountBindVo> list = service.getAccountsByMerchant(merchantNo);
        return Result.success(list);
    }

    @GetMapping("/listByAccount")
    public Result listByAccount(@RequestParam String accountNo) {
        List<MerchantAccountBindVo> list = service.getMerchantsByAccount(accountNo);
        return Result.success(list);
    }
}
