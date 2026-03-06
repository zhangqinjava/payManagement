package com.al.controller;

import com.al.bean.vo.MerchantTerminalBindVo;
import com.al.service.MerchantTerminalBindService;
import com.al.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/terminal")
public class MerchantTerminalBindController {

    private final MerchantTerminalBindService service;

    public MerchantTerminalBindController(MerchantTerminalBindService service) {
        this.service = service;
    }

    @PostMapping("/bind")
    public Result bind(@RequestBody MerchantTerminalBindVo bind) throws Exception {
        service.bindTerminal(bind);
        return Result.success("绑定成功");
    }

    @PostMapping("/unbind")
    public Result unbind(@RequestParam String merchantNo, @RequestParam String terminalNo,
                         @RequestParam String updateUser) {
        service.unbindTerminal(merchantNo, terminalNo, updateUser);
        return Result.success("解绑成功");
    }

    @GetMapping("/list")
    public Result list(@RequestParam String merchantNo) {
        List<MerchantTerminalBindVo> list = service.getTerminalsByMerchant(merchantNo);
        return Result.success(list);
    }

    @GetMapping("/getMerchant")
    public Result getMerchant(@RequestParam String terminalNo) {
        MerchantTerminalBindVo bind = service.getMerchantByTerminal(terminalNo);
        return Result.success(bind);
    }
}
