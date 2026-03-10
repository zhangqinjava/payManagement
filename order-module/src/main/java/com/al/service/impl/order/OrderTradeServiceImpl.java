package com.al.service.impl.order;

import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.common.business.AccountTradeEnum;
import com.al.common.business.TopicEnum;
import com.al.config.ChannelRouter;
import com.al.config.RocketMQUtil;
import com.al.config.factory.ChannelFactory;
import com.al.fegin.account.AccountFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderTradeService;
import com.al.service.channel.TradeChannel;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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
    private ChannelFactory channelFactory;
    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private RocketMQUtil rocketMQUtil;


    @Transactional(rollbackFor = Exception.class)
    public OrderTradeVo createAndPay(OrderTradeVo req,Object result) throws Exception {
        try {
            // 1. 更新最终状态
            if (result.equals(TradeStatusEnum.SUCCESS.getCode())) {
                orderTradeMapper.updateStatus(
                        req.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.SUCCESS.getCode(),
                        null
                );
                //2.异步上账
                rocketMQUtil.send(TopicEnum.ACCOUNT_UP.getTopic(), "*", req.getTradeNo(), req);
            } else {
                orderTradeMapper.updateStatus(
                        req.getTradeNo(),
                        TradeStatusEnum.PAYING.getCode(),
                        TradeStatusEnum.FAIL.getCode(),
                        "失败"//result,getRetMsg()
                );
            }
            return req;
        } catch (Exception e) {
            log.error("交易订单调用通道报错:{}");
            throw e;
        }
    }
    @Transactional
    @Override
    public OrderTradeVo createOrder(OrderTradeDto req) throws Exception {
        try {
            log.info("start create order infomation:{}",req);
            OrderTradeVo order = buildOrderTradeVo(req);
            log.info("assembly order infomation:{}",order);
            orderTradeMapper.insert(order);
            orderTradeMapper.updateStatus(
                    order.getTradeNo(),
                    TradeStatusEnum.INIT.getCode(),
                    TradeStatusEnum.PAYING.getCode(),
                    null
            );
            return order;
        }catch (Exception e) {
            log.error("create order error:{}", e.getMessage());
            throw e;
        }
    }
    private OrderTradeVo buildOrderTradeVo(OrderTradeDto req) throws Exception {
        return OrderTradeVo.builder()
                .tradeNo(TraceUtil.createTraceId())
                .merchantNo(req.getMerchantNo())
                .orderNo(req.getOrderNo())
                .orderDate(req.getOrderDate())
                .bizType(req.getBizType())
                .payAmount(req.getAmount())
                .channelAmount(BigDecimal.ZERO)
                .channelTrace(TraceUtil.createTraceId())//请求通道流水号
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
