package com.al.account.controller;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountUpDto;
import com.al.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/operation")
public class AccountBanlanceController {
    @PostMapping("/up")
    public Result up(@RequestBody @Valid AccountUpDto accountUpDto) throws Exception {
        return Result.success(null);
    }
    @PostMapping("/down")
    public Result down(@RequestBody @Valid AccountUpDto accountUpDto ) throws Exception {
        return Result.success(null);
    }
    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid AccountDto accountDto) throws Exception {
        return Result.success(null);
    }
}
