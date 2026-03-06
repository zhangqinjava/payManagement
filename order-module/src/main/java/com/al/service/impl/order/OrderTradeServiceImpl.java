package com.al.service.impl.order;

import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.config.factory.ChannelFactory;
import com.al.fegin.account.AccountFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderTradeService;
import com.al.service.channel.TradeChannel;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@Slf4j
public class OrderTradeServiceImpl implements OrderTradeService {
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private ChannelFactory channelFactory;
    @Autowired
    private AccountFeginClient accountFeginClient;

    @Transactional(rollbackFor = Exception.class)
    public OrderTradeVo createAndPay(OrderTradeDto req) throws Exception {
        try {
            // 1. 创建交易单
            OrderTradeVo build = OrderTradeVo.builder()
                    .tradeNo(TraceUtil.createTraceId())
                    .merchantNo(req.getMerchantNo())
                    .orderNo(req.getOrderNo())
                    .orderDate(req.getOrderDate())
                    .bizType(req.getBizType())
                    .payAmount(req.getAmount())
                    .payChannel(req.getPayChannel())
                    .tradeStatus(TradeStatusEnum.INIT.getCode())
                    .requestTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            orderTradeMapper.insert(build);
            // 3. 更新为支付中
            orderTradeMapper.updateStatus(
                    build.getTradeNo(),
                    TradeStatusEnum.INIT.getCode(),
                    TradeStatusEnum.PAYING.getCode(),
                    null
            );
            // 4. 调用支付渠道
            TradeChannel channel = channelFactory.route(req.getPayChannel());
            Object result = channel.pay(null);

            // 5. 更新最终状态
            if (result.equals(TradeStatusEnum.SUCCESS.getCode())) {
                orderTradeMapper.updateStatus(
                        build.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.SUCCESS.getCode(),
                        null
                );
                accountFeginClient.up(null);
            } else {
                orderTradeMapper.updateStatus(
                        build.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.FAIL.getCode(),
                        "失败"
                );
            }
            return build;
        } catch (Exception e) {
            log.error("交易订单调用通道报错:{}");
            throw e;
        }
    }

    @Override
    public void handleCallback(String tradeNo, String channelStatus) {
        // 1. 查询订单
        OrderTradeVo order = orderTradeMapper.selectOne(Wrappers.lambdaQuery(OrderTradeVo.class).eq(OrderTradeVo::getTradeNo, tradeNo));
        if (order == null) {
            throw new BusinessException("订单不存在: " + tradeNo);
        }

        // 2. 幂等处理：已处理状态直接返回
        if (order.getTradeStatus() == TradeStatusEnum.SUCCESS.getCode()
                || order.getTradeStatus() == TradeStatusEnum.FAIL.getCode()) {
                 throw new BusinessException( "订单已经被修改");
        }

        // 3. 根据通道状态更新订单
        if ("SUCCESS".equalsIgnoreCase(channelStatus)) {
            order.setTradeStatus(TradeStatusEnum.SUCCESS.getCode());
            order.setUpdateTime(LocalDateTime.now());
            orderTradeMapper.update(order,Wrappers.<OrderTradeVo>lambdaUpdate()
                    .eq(OrderTradeVo::getTradeNo, tradeNo)
                    .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                    .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
//            // 4. 更新账户余额和流水
//            MerchantAccount account = accountService.getByMerchantNo(order.getMerchantNo());
//            // 假设支付时已经冻结金额
//            account.setBalance(account.getBalance().add(order.getPayAmount()));
//            account.setFrozenBalance(account.getFrozenBalance().subtract(order.getPayAmount()));
//            accountService.update(account);
//
//            accountFlowMapper.insert(new AccountFlow(
//                    order.getMerchantNo(),
//                    tradeNo,
//                    order.getPayAmount(),
//                    FlowType.PAY_SUCCESS
//            ));

        } else if ("FAIL".equalsIgnoreCase(channelStatus)) {
            order.setTradeStatus(TradeStatusEnum.FAIL.getCode());
            order.setUpdateTime(LocalDateTime.now());
            orderTradeMapper.update(order,Wrappers.<OrderTradeVo>lambdaUpdate()
                    .eq(OrderTradeVo::getTradeNo, tradeNo)
                    .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                    .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));

//            // 退款或解冻冻结资金
//            MerchantAccount account = accountService.getByMerchantNo(order.getMerchantNo());
//            account.setFrozenBalance(account.getFrozenBalance().subtract(order.getPayAmount()));
//            accountService.update(account);
//
//            accountFlowMapper.insert(new AccountFlow(
//                    order.getMerchantNo(),
//                    tradeNo,
//                    order.getPayAmount(),
//                    FlowType.PAY_FAIL
//            ));
        } else {
            log.warn("未知回调状态: {} for order {}", channelStatus, tradeNo);
        }

        // 5. 异步通知商户（入消息队列）
//        merchantNotifyService.asyncNotify(order);
    }

    @Override
    public OrderTradeVo query(String tradeNo) {
        return null;
    }
}
