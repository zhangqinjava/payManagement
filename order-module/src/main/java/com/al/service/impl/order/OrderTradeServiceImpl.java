package com.al.service.impl.order;

import com.al.bean.business.ChannelResultHelper;
import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.common.business.AccountTradeEnum;
import com.al.common.business.TopicEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.config.RocketMQUtil;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderTradeService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class OrderTradeServiceImpl implements OrderTradeService {

    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private RocketMQUtil rocketMQUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderTradeVo createAndPay(OrderTradeVo order, Object channelResult) throws Exception {
        try {
            boolean paySuccess = ChannelResultHelper.isPaySuccess(channelResult);
            if (paySuccess) {
                orderTradeMapper.updateStatus(
                        order.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.SUCCESS.getCode(),
                        null
                );
                order.setTradeStatus(TradeStatusEnum.SUCCESS.getCode());
                order.setSuccessTime(LocalDateTime.now());
                rocketMQUtil.send(TopicEnum.ACCOUNT_UP.getTopic(), "*", order.getAccountFlow(), order);
                log.info("支付成功，已发送上账消息 tradeNo={}", order.getTradeNo());
            } else {
                String failReason = channelResult == null ? "渠道无响应" : channelResult.toString();
                orderTradeMapper.updateStatus(
                        order.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.FAIL.getCode(),
                        failReason
                );
                order.setTradeStatus(TradeStatusEnum.FAIL.getCode());
                order.setFailReason(failReason);
                log.warn("支付失败 tradeNo={}, reason={}", order.getTradeNo(), failReason);
            }
            order.setUpdateTime(LocalDateTime.now());
            return order;
        } catch (Exception e) {
            log.error("交易订单更新状态失败 tradeNo={}", order.getTradeNo(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderTradeVo createOrder(OrderTradeDto req) throws Exception {
        log.info("start create order information:{}", req);
        OrderTradeVo order = buildOrderTradeVo(req);
        orderTradeMapper.insert(order);
        orderTradeMapper.updateStatus(
                order.getTradeNo(),
                TradeStatusEnum.INIT.getCode(),
                TradeStatusEnum.PAYING.getCode(),
                null
        );
        order.setTradeStatus(TradeStatusEnum.PAYING.getCode());
        return order;
    }

    private OrderTradeVo buildOrderTradeVo(OrderTradeDto req) {
        LocalDateTime orderDate = req.getOrderDate() != null ? req.getOrderDate() : LocalDateTime.now();
        return OrderTradeVo.builder()
                .tradeNo(TraceUtil.createTraceId())
                .merchantNo(req.getMerchantNo())
                .orderNo(req.getOrderNo())
                .orderDate(orderDate)
                .bizType(req.getBizType())
                .payAmount(req.getAmount())
                .channelAmount(BigDecimal.ZERO)
                .channelTrace(TraceUtil.createTraceId())
                .payChannel(req.getPayChannel())
                .tradeStatus(TradeStatusEnum.INIT.getCode())
                .accountFlow(TraceUtil.createTraceId())
                .accountStatus(AccountTradeEnum.INIT.getCode())
                .requestTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(String tradeNo, String channelStatus) {
        OrderTradeVo order = orderTradeMapper.selectOne(
                Wrappers.lambdaQuery(OrderTradeVo.class).eq(OrderTradeVo::getTradeNo, tradeNo));
        if (order == null) {
            throw new BusinessException("订单不存在: " + tradeNo);
        }
        if (TradeStatusEnum.SUCCESS.getCode().equals(order.getTradeStatus())
                || TradeStatusEnum.FAIL.getCode().equals(order.getTradeStatus())) {
            log.info("订单已终态，忽略回调 tradeNo={}", tradeNo);
            return;
        }
        if ("SUCCESS".equalsIgnoreCase(channelStatus)) {
            orderTradeMapper.updateStatus(
                    tradeNo,
                    TradeStatusEnum.PAYING.getCode(),
                    TradeStatusEnum.SUCCESS.getCode(),
                    null
            );
            if (!AccountTradeEnum.SUCESS.getCode().equals(order.getAccountStatus())) {
                rocketMQUtil.send(TopicEnum.ACCOUNT_UP.getTopic(), "*", order.getAccountFlow(), order);
            }
        } else if ("FAIL".equalsIgnoreCase(channelStatus)) {
            orderTradeMapper.updateStatus(
                    tradeNo,
                    TradeStatusEnum.PAYING.getCode(),
                    TradeStatusEnum.FAIL.getCode(),
                    "渠道回调失败"
            );
        } else {
            log.warn("未知回调状态: {} for order {}", channelStatus, tradeNo);
        }
    }

    @Override
    public OrderTradeVo query(String tradeNo) {
        if (StringUtils.isBlank(tradeNo)) {
            throw new BusinessException("交易号不能为空");
        }
        OrderTradeVo order = orderTradeMapper.selectOne(
                Wrappers.lambdaQuery(OrderTradeVo.class).eq(OrderTradeVo::getTradeNo, tradeNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }
}
