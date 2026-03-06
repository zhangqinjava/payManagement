package com.al.controller;

import com.al.bean.dto.OrderTradeDto;
import com.al.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@Slf4j
public class OrderController {
    @PostMapping("/create")
    public Result create(OrderTradeDto orderTradeDto) {
        return Result.success(null);

    }
}
