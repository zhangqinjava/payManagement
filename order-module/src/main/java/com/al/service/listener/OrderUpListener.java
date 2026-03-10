package com.al.service.listener;

import com.al.bean.dto.account.AccountUpDownDto;
import com.al.bean.dto.merchant.MerchantFeeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.account.AccountUpDownVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.bean.vo.merchant.MerchantFeeVo;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Wrapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "ACCOUNT_TOPIC_UP",
        selectorExpression = "*",
        consumerGroup = "order_pay_group"
)public class OrderUpListener implements RocketMQListener<OrderTradeVo> {
    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Resource(name = "asyncThreadConfig")
    private ThreadPoolExecutor threadPoolExecutor;
    @Override
    public void onMessage(OrderTradeVo orderTradeVo) {
        log.info("start execute consumer :{}",orderTradeVo);
        try {
            OrderTradeVo order = orderTradeMapper.selectOne(Wrappers.lambdaQuery(OrderTradeVo.class)
                    .eq(OrderTradeVo::getOrderNo, orderTradeVo.getOrderNo()));
            if (order == null) {
                log.error("order not exist:{}", orderTradeVo.getOrderNo());
                return;
            }
            // 幂等判断
            if(order.getAccountStatus() == AccountTradeEnum.SUCESS.getCode()){
                log.info("account already success skip:{}", orderTradeVo.getOrderNo());
                return;
            }
            Result<List<MerchantAccountBindVo>> listResult = merchantFeginClient.listByMerchant(orderTradeVo.getMerchantNo(), BusiEnum.SETTLE.getCode());
            if (CollectionUtils.isEmpty(listResult.getData())) {
                throw new BusinessException("没有查询到商户绑定的账户信息");
            }
            MerchantAccountBindVo merchantAccountBindVo = listResult.getData().get(0);
            AccountUpDownDto account=new AccountUpDownDto();
            account.setAccountNo(merchantAccountBindVo.getAccountNo());
            account.setAccountType(BusiEnum.SETTLE.getCode());
            account.setMerchantNo(orderTradeVo.getMerchantNo());
            account.setAmount(orderTradeVo.getPayAmount().toString());
            account.setBizOrderDate(orderTradeVo.getOrderDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            account.setBizOrderTime(orderTradeVo.getOrderDate().format(DateTimeFormatter.ofPattern("HHmmss")));
            account.setBizOrderNo(orderTradeVo.getOrderNo());
            account.setBizType(orderTradeVo.getBizType());
            account.setFlowNo(orderTradeVo.getAccountFlow());
            account.setFunCode(BusiEnum.FUNCODE_UP.getCode());
            Result<AccountUpDownVo> up = accountFeginClient.up(account);
            if (up.getCode() == ResultEnum.SUCESS.getCode()) {
                OrderTradeVo build = OrderTradeVo.builder()
                        .accountStatus(AccountTradeEnum.SUCESS.getCode())
                        .accountTime(LocalDateTime.now())
                        .build();
                orderTradeMapper.update(build, Wrappers.lambdaUpdate(OrderTradeVo.class)
                        .eq(OrderTradeVo::getOrderNo, orderTradeVo.getOrderNo())
                        .eq(OrderTradeVo::getMerchantNo, orderTradeVo.getMerchantNo()));
                log.info("current consumer update account success status complte:{]",orderTradeVo);
            }else{
                OrderTradeVo build = OrderTradeVo.builder().accountStatus(AccountTradeEnum.FAIL.getCode()).build();
                orderTradeMapper.update(build, Wrappers.lambdaUpdate(OrderTradeVo.class)
                        .eq(OrderTradeVo::getOrderNo, orderTradeVo.getOrderNo()));
                log.info("current consumer update account fail status complte:{]",orderTradeVo);
            }
        } catch (Exception e) {
            log.error("consumer executer message failed:{}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void caculateFee(OrderTradeVo order) throws Exception {
        CompletableFuture.supplyAsync(() -> {
            try {
                MerchantFeeDto dto = new MerchantFeeDto();
                dto.setBizType(order.getBizType());
                dto.setMerchantNo(order.getMerchantNo());
                dto.setEffectiveTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                dto.setStatus(Integer.valueOf(BusiEnum.RATE_NOT_DISABLED.getCode()));
                Result<List<MerchantFeeVo>> feeList = null;
                feeList = merchantFeginClient.queryFee(dto);
                if (CollectionUtils.isEmpty(feeList.getData())) {
                    return null;
                }
                log.info("current consumer :{} select merchantno :{]fee rate  :{]",order.getOrderNo(),order.getMerchantNo(),feeList.getData().get(0));
                return feeList.getData().get(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },threadPoolExecutor).thenApply(result->{
            return null;
        });

    }
}
