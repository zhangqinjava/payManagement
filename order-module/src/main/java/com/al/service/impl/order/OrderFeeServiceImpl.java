package com.al.service.impl.order;

import com.al.bean.dto.billing.BillingSplitCalculateDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.billing.BillingSplitCalculateVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.AccountTradeEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.billing.BillingFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderFeeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class OrderFeeServiceImpl implements OrderFeeService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private BillingFeginClient billingFeginClient;
    @Autowired
    private OrderTradeMapper orderTradeMapper;

    @Override
    public BillingSplitCalculateVo calculateUpfrontFee(OrderTradeVo order) {
        BillingSplitCalculateDto dto = new BillingSplitCalculateDto();
        dto.setMerchantNo(order.getMerchantNo());
        dto.setBizType(order.getBizType());
        dto.setOrderNo(order.getOrderNo());
        dto.setCalculateDate(LocalDate.now().format(DATE_FMT));
        dto.setAmount(order.getPayAmount());
        Result<BillingSplitCalculateVo> result = billingFeginClient.calculateForSplit(dto);
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode() || result.getData() == null) {
            String msg = result != null ? result.getMsg() : "计费服务无响应";
            throw new BusinessException("前项手续费计算失败:" + msg);
        }
        BillingSplitCalculateVo feeResult = result.getData();
        if (feeResult.getFeeAmount() == null) {
            feeResult.setFeeAmount(BigDecimal.ZERO);
        }
        if (feeResult.getNetAmount() == null) {
            feeResult.setNetAmount(order.getPayAmount().subtract(feeResult.getFeeAmount()));
        }
        if (feeResult.getNetAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("手续费超过订单金额");
        }
        log.info("前项手续费计算完成 orderNo={}, fee={}, net={}",
                order.getOrderNo(), feeResult.getFeeAmount(), feeResult.getNetAmount());
        return feeResult;
    }

    @Override
    public void saveFeeSnapshot(OrderTradeVo order, BillingSplitCalculateVo feeResult, String feeFlow) {
        OrderTradeVo update = OrderTradeVo.builder()
                .feeAmount(feeResult.getFeeAmount())
                .netAmount(feeResult.getNetAmount())
                .feeFlow(feeFlow)
                .feeStatus(AccountTradeEnum.INIT.getCode())
                .updateTime(LocalDateTime.now())
                .build();
        orderTradeMapper.update(update, Wrappers.lambdaUpdate(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderTradeVo::getMerchantNo, order.getMerchantNo()));
        order.setFeeAmount(feeResult.getFeeAmount());
        order.setNetAmount(feeResult.getNetAmount());
        order.setFeeFlow(feeFlow);
        order.setFeeStatus(AccountTradeEnum.INIT.getCode());
    }
}
