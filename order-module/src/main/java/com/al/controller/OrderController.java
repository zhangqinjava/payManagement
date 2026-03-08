package com.al.controller;

import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.common.Result;
import com.al.common.business.TopicEnum;
import com.al.common.util.TraceUtil;
import com.al.config.RocketMQUtil;
import com.al.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RocketMQUtil rocketMQUtil;
    @PostMapping("/create")
    public Result create(@RequestBody OrderTradeDto orderTradeDto) throws Exception {
        return Result.success(orderService.create(orderTradeDto));
    }

}
