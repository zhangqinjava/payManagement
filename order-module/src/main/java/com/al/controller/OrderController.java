package com.al.controller;

import com.al.bean.dto.OrderQueryDto;
import com.al.bean.dto.OrderRefundTradeDto;
import com.al.bean.dto.OrderTradeDto;
import com.al.common.Result;
import com.al.service.order.OrderRefundService;
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
    @Autowired
    private OrderRefundService orderRefundService;

    @PostMapping("/create")
    public Result create(@Valid @RequestBody OrderTradeDto orderTradeDto) throws Exception {
        return Result.success(orderService.create(orderTradeDto));
    }

    @PostMapping("/query")
    public Result query(@RequestBody @Valid OrderQueryDto orderQueryDto) throws Exception {
        return Result.success(orderService.query(orderQueryDto));
    }

    @GetMapping("/queryByOrderNo")
    public Result queryByOrderNo(@RequestParam String orderNo) throws Exception {
        return Result.success(orderService.queryByOrderNo(orderNo));
    }

    @PostMapping("/update")
    public Result update(@RequestParam String orderNo) throws Exception {
        return Result.success(orderService.updateStatus(orderNo));
    }

    @PostMapping("/refund")
    public Result refund(@Valid @RequestBody OrderRefundTradeDto dto) throws Exception {
        return Result.success(orderRefundService.createRefundOrder(dto));
    }
}
