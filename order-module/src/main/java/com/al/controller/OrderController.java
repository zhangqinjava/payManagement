package com.al.controller;

import com.al.bean.dto.OrderQueryDto;
import com.al.bean.dto.OrderTradeDto;
import com.al.common.Result;
import com.al.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/business")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/create")
    public Result create(@Valid  @RequestBody OrderTradeDto orderTradeDto) throws Exception {
        return Result.success(orderService.create(orderTradeDto));
    }
    @PostMapping("/query")
    public Result query(@RequestBody @Valid OrderQueryDto orderQueryDto) throws Exception {
        return Result.success(orderService.query(orderQueryDto));
    }
    @PostMapping("/update")
    public Result update(@Valid @RequestBody OrderTradeDto orderTradeDto) throws Exception {
        return Result.success(null);
    }

}
