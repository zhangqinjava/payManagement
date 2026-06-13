package com.al.service.listener;

import com.al.bean.dto.account.AccountUpDownDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.account.AccountUpDownVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.AccountTradeEnum;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.account.AccountFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "ACCOUNT_TOPIC_UP",
        selectorExpression = "*",
        consumerGroup = "order_pay_group"
)
public class OrderUpListener implements RocketMQListener<OrderTradeVo> {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Autowired
    private OrderTradeMapper orderTradeMapper;

    @Override
    public void onMessage(OrderTradeVo message) {
        log.info("start execute account up consumer:{}", message);
        try {
            OrderTradeVo order = orderTradeMapper.selectOne(Wrappers.lambdaQuery(OrderTradeVo.class)
                    .eq(OrderTradeVo::getOrderNo, message.getOrderNo())
                    .eq(OrderTradeVo::getMerchantNo, message.getMerchantNo()));
            if (order == null) {
                log.error("order not exist:{}", message.getOrderNo());
                return;
            }
            if (AccountTradeEnum.SUCESS.getCode().equals(order.getAccountStatus())) {
                log.info("account already success skip:{}", message.getOrderNo());
                return;
            }
            AccountUpDownDto upDto = buildUpDto(order);
            Result<AccountUpDownVo> upResult = accountFeginClient.up(upDto);
            log.info("request account response:{}", upResult);
            updateAccountStatus(order, upResult);
        } catch (Exception e) {
            log.error("consumer execute message failed:{}", e.getMessage(), e);
            throw new BusinessException("订单上账消费失败: " + e.getMessage());
        }
    }

    private AccountUpDownDto buildUpDto(OrderTradeVo order) throws Exception {
        Result<java.util.List<MerchantAccountBindVo>> listResult = merchantFeginClient.listByMerchant(
                order.getMerchantNo(), BusiEnum.CASH.getCode());
        if (listResult == null || listResult.getCode() != ResultEnum.SUCESS.getCode()
                || CollectionUtils.isEmpty(listResult.getData())) {
            throw new BusinessException("没有查询到商户绑定的账户信息");
        }
        MerchantAccountBindVo bind = listResult.getData().get(0);
        LocalDateTime orderTime = order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now();
        AccountUpDownDto account = new AccountUpDownDto();
        account.setAccountNo(bind.getAccountNo().trim());
        account.setAccountType(bind.getAccountType());
        account.setMerchantNo(order.getMerchantNo());
        account.setAmount(order.getPayAmount().toPlainString());
        account.setBizOrderDate(orderTime.format(DATE_FMT));
        account.setBizOrderTime(orderTime.format(TIME_FMT));
        account.setBizOrderNo(order.getOrderNo());
        account.setBizType(order.getBizType());
        account.setChannelCode(order.getPayChannel());
        account.setFlowNo(order.getAccountFlow());
        account.setFunCode(BusiEnum.FUNCODE_UP.getCode());
        return account;
    }

    private void updateAccountStatus(OrderTradeVo order, Result<AccountUpDownVo> upResult) {
        boolean success = upResult != null && upResult.getCode() == ResultEnum.SUCESS.getCode();
        OrderTradeVo update = OrderTradeVo.builder()
                .accountStatus(success ? AccountTradeEnum.SUCESS.getCode() : AccountTradeEnum.FAIL.getCode())
                .accountTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderTradeMapper.update(update, Wrappers.lambdaUpdate(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
        log.info("order account status updated, orderNo={}, success={}", order.getOrderNo(), success);
    }
}
