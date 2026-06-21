package com.al.service.listener;

import com.al.bean.dto.account.AccountTransferDto;
import com.al.bean.dto.account.AccountUpDownDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.account.AccountTransferVo;
import com.al.bean.vo.account.AccountUpDownVo;
import com.al.bean.vo.billing.BillingSplitCalculateVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.AccountTradeEnum;
import com.al.common.business.BusiEnum;
import com.al.common.business.TopicEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.config.RocketMQUtil;
import com.al.fegin.account.AccountFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderFeeService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    @Autowired
    private RocketMQUtil rocketMQUtil;
    @Autowired
    private OrderFeeService orderFeeService;

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

            BillingSplitCalculateVo feeResult = orderFeeService.calculateUpfrontFee(order);
            String feeFlow = TraceUtil.createTraceId();
            orderFeeService.saveFeeSnapshot(order, feeResult, feeFlow);

            MerchantAccountBindVo cashAccount = resolveCashAccount(order.getMerchantNo());
            Result<AccountUpDownVo> upResult = accountFeginClient.up(buildUpDto(order, cashAccount));
            log.info("request account up response:{}", upResult);
            if (!isSuccess(upResult)) {
                markAccountFail(order);
                return;
            }

            boolean feeDeducted = deductUpfrontFee(order, cashAccount, feeResult.getFeeAmount(), feeFlow);
            if (!feeDeducted) {
                markAccountFail(order);
                return;
            }

            markAccountSuccess(order);
            rocketMQUtil.send(TopicEnum.ORDER_SPLIT.getTopic(), "*", order.getOrderNo(), order);
            log.info("account up and upfront fee done, split message sent orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("consumer execute message failed:{}", e.getMessage(), e);
            throw new BusinessException("订单上账消费失败: " + e.getMessage());
        }
    }

    private boolean deductUpfrontFee(OrderTradeVo order, MerchantAccountBindVo cashAccount,
                                     BigDecimal feeAmount, String feeFlow) throws Exception {
        if (feeAmount == null || feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            updateFeeStatus(order, AccountTradeEnum.SUCESS.getCode());
            return true;
        }
        MerchantAccountBindVo feeAccount = resolveFeeAccount(order.getMerchantNo(), cashAccount);
        AccountTransferDto transferDto = buildFeeTransferDto(order, cashAccount, feeAccount, feeAmount, feeFlow);
        Result<AccountTransferVo> transferResult = accountFeginClient.transfer(transferDto);
        log.info("upfront fee transfer response:{}", transferResult);
        if (isSuccess(transferResult)) {
            updateFeeStatus(order, AccountTradeEnum.SUCESS.getCode());
            return true;
        }
        updateFeeStatus(order, AccountTradeEnum.FAIL.getCode());
        return false;
    }

    private AccountUpDownDto buildUpDto(OrderTradeVo order, MerchantAccountBindVo bind) {
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
        account.setRemark("支付上账");
        return account;
    }

    private AccountTransferDto buildFeeTransferDto(OrderTradeVo order, MerchantAccountBindVo cashAccount,
                                                   MerchantAccountBindVo feeAccount, BigDecimal feeAmount,
                                                   String feeFlow) {
        LocalDateTime orderTime = order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now();
        AccountTransferDto dto = new AccountTransferDto();
        dto.setFlowNo(feeFlow);
        dto.setOutAccountNo(cashAccount.getAccountNo().trim());
        dto.setOutMerchantNo(order.getMerchantNo());
        dto.setOutAccountType(cashAccount.getAccountType());
        dto.setInAccountNo(feeAccount.getAccountNo().trim());
        dto.setInMerchantNo(feeAccount.getMerchantNo());
        dto.setInAccountType(feeAccount.getAccountType());
        dto.setChannelCode(order.getPayChannel());
        dto.setBizType(order.getBizType());
        dto.setBizOrderNo(order.getOrderNo());
        dto.setBizOrderDate(orderTime.format(DATE_FMT));
        dto.setBizOrderTime(orderTime.format(TIME_FMT));
        dto.setAmount(feeAmount.toPlainString());
        dto.setFunCode(BusiEnum.FUNCODE_TRANSFER.getCode());
        dto.setRemark("前项手续费");
        return dto;
    }

    private MerchantAccountBindVo resolveCashAccount(String merchantNo) throws Exception {
        Result<java.util.List<MerchantAccountBindVo>> listResult = merchantFeginClient.listByMerchant(
                merchantNo, BusiEnum.CASH.getCode());
        if (listResult == null || listResult.getCode() != ResultEnum.SUCESS.getCode()
                || CollectionUtils.isEmpty(listResult.getData())) {
            throw new BusinessException("没有查询到商户绑定的现金账户");
        }
        return listResult.getData().get(0);
    }

    private MerchantAccountBindVo resolveFeeAccount(String merchantNo, MerchantAccountBindVo cashAccount) throws Exception {
        Result<java.util.List<MerchantAccountBindVo>> listResult = merchantFeginClient.listByMerchant(
                merchantNo, BusiEnum.SETTLE.getCode());
        if (listResult != null && listResult.getCode() == ResultEnum.SUCESS.getCode()
                && CollectionUtils.isNotEmpty(listResult.getData())) {
            return listResult.getData().get(0);
        }
        MerchantAccountBindVo fallback = new MerchantAccountBindVo();
        fallback.setMerchantNo(merchantNo);
        fallback.setAccountNo(cashAccount.getAccountNo());
        fallback.setAccountType(BusiEnum.SETTLE.getCode());
        return fallback;
    }

    private void markAccountSuccess(OrderTradeVo order) {
        OrderTradeVo update = OrderTradeVo.builder()
                .accountStatus(AccountTradeEnum.SUCESS.getCode())
                .accountTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderTradeMapper.update(update, Wrappers.lambdaUpdate(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
    }

    private void markAccountFail(OrderTradeVo order) {
        OrderTradeVo update = OrderTradeVo.builder()
                .accountStatus(AccountTradeEnum.FAIL.getCode())
                .accountTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderTradeMapper.update(update, Wrappers.lambdaUpdate(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
    }

    private void updateFeeStatus(OrderTradeVo order, Integer feeStatus) {
        OrderTradeVo update = OrderTradeVo.builder()
                .feeStatus(feeStatus)
                .updateTime(LocalDateTime.now())
                .build();
        orderTradeMapper.update(update, Wrappers.lambdaUpdate(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
        order.setFeeStatus(feeStatus);
    }

    private boolean isSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultEnum.SUCESS.getCode();
    }
}
