package com.ai.controller;

import com.ai.bean.dto.CaculateDto;
import com.ai.service.CaculateService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/caculate")
public class CaculateController{
    @Autowired
    private CaculateService caculateService;
    @PostMapping("/fee")
    public Result caculate(@Valid @RequestBody CaculateDto dto) throws Exception {
        return Result.success(caculateService.caculate(dto));
    }
}
