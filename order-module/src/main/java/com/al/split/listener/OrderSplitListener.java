package com.al.split.listener;

import com.al.bean.vo.OrderTradeVo;
import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.service.OrderSplitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "ORDER_TOPIC_SPLIT",
        selectorExpression = "*",
        consumerGroup = "order_split_group"
)
public class OrderSplitListener implements RocketMQListener<OrderTradeVo> {

    @Autowired
    private OrderSplitService orderSplitService;

    @Override
    public void onMessage(OrderTradeVo order) {
        log.info("start order split consumer:{}", order);
        try {
            OrderSplitRequestDto request = new OrderSplitRequestDto();
            request.setOrderNo(order.getOrderNo());
            request.setMerchantNo(order.getMerchantNo());
            orderSplitService.executeSplit(request);
        } catch (Exception e) {
            log.error("order split consumer failed orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException(e);
        }
    }
}
