package com.al.service.listener;

import com.al.bean.business.TradeStatusEnum;
import com.al.bean.vo.OrderTradeVo;
import com.al.mapper.OrderTradeMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQTransactionListener()
public class OrderTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        OrderTradeVo orderTradeVo = (OrderTradeVo) arg;
        try {
            int i = orderTradeMapper.updateStatus(
                    orderTradeVo.getTradeNo(),
                    TradeStatusEnum.PAYING.getCode(),
                    TradeStatusEnum.SUCCESS.getCode(),
                    null
            );
            log.info("执行本地事务, tradeNo={}, result={}", orderTradeVo.getTradeNo(), i);
            if (i <= 0) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("更新订单状态异常:{}", e.getMessage());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        OrderTradeVo orderTradeVo = (OrderTradeVo) message.getPayload();
        OrderTradeVo result = orderTradeMapper.selectOne(Wrappers.lambdaQuery(OrderTradeVo.class)
                .eq(OrderTradeVo::getTradeNo, orderTradeVo.getTradeNo()));
        if (result == null) {
            log.warn("订单不存在，tradeNo={}", orderTradeVo.getTradeNo());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        if (TradeStatusEnum.SUCCESS.getCode().equals(result.getTradeStatus())) {
            return RocketMQLocalTransactionState.COMMIT;
        } else if (TradeStatusEnum.FAIL.getCode().equals(result.getTradeStatus())) {
            return RocketMQLocalTransactionState.ROLLBACK;
        } else {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
