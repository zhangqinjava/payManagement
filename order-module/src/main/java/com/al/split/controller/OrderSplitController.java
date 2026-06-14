package com.al.split.controller;

import com.al.common.Result;
import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.service.OrderSplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/split")
public class OrderSplitController {

    @Autowired
    private OrderSplitService orderSplitService;

    @PostMapping("/execute")
    public Result execute(@Valid @RequestBody OrderSplitRequestDto request) throws Exception {
        return Result.success(orderSplitService.executeSplit(request));
    }

    @GetMapping("/query")
    public Result query(@RequestParam String orderNo, @RequestParam String merchantNo) {
        return Result.success(orderSplitService.queryByOrderNo(orderNo, merchantNo));
    }
}
