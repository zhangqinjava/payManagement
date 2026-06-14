package com.al.split.service.impl;

import com.al.bean.dto.billing.BillingSplitCalculateDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.billing.BillingSplitCalculateVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.billing.BillingFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.bean.dto.SplitReceiverDto;
import com.al.split.enums.SplitReceiverTypeEnum;
import com.al.split.model.SplitPlanLine;
import com.al.split.service.SplitRuleService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SplitRuleServiceImpl implements SplitRuleService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private BillingFeginClient billingFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;

    @Override
    public BigDecimal calculateFee(OrderTradeVo order) throws Exception {
        BillingSplitCalculateDto dto = new BillingSplitCalculateDto();
        dto.setMerchantNo(order.getMerchantNo());
        dto.setBizType(order.getBizType());
        dto.setOrderNo(order.getOrderNo());
        dto.setCalculateDate(LocalDate.now().format(DATE_FMT));
        dto.setAmount(order.getPayAmount());
        try {
            Result<BillingSplitCalculateVo> result = billingFeginClient.calculateForSplit(dto);
            if (result != null && result.getCode() == ResultEnum.SUCESS.getCode()
                    && result.getData() != null && result.getData().getFeeAmount() != null) {
                return result.getData().getFeeAmount();
            }
        } catch (Exception e) {
            log.warn("清分计费失败，按零手续费处理 orderNo={}", order.getOrderNo(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<SplitPlanLine> buildDefaultPlan(OrderTradeVo order, BigDecimal feeAmount) throws Exception {
        MerchantAccountBindVo cashAccount = resolveAccount(order.getMerchantNo(), BusiEnum.CASH.getCode());
        MerchantAccountBindVo settleAccount = resolveSettleAccount(order.getMerchantNo(), cashAccount);
        BigDecimal fee = feeAmount == null ? BigDecimal.ZERO : feeAmount;
        BigDecimal net = order.getPayAmount().subtract(fee);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("手续费超过订单金额");
        }
        List<SplitPlanLine> plan = new ArrayList<>();
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            plan.add(SplitPlanLine.builder()
                    .receiverMerchantNo(settleAccount.getMerchantNo())
                    .receiverAccountNo(settleAccount.getAccountNo())
                    .receiverAccountType(settleAccount.getAccountType())
                    .amount(fee)
                    .splitType(SplitReceiverTypeEnum.PLATFORM_FEE.getCode())
                    .remark("平台手续费")
                    .build());
        }
        if (net.compareTo(BigDecimal.ZERO) > 0) {
            plan.add(SplitPlanLine.builder()
                    .receiverMerchantNo(settleAccount.getMerchantNo())
                    .receiverAccountNo(settleAccount.getAccountNo())
                    .receiverAccountType(settleAccount.getAccountType())
                    .amount(net)
                    .splitType(SplitReceiverTypeEnum.MERCHANT_SETTLE.getCode())
                    .remark("商户待清分")
                    .build());
        }
        return plan;
    }

    @Override
    public List<SplitPlanLine> buildCustomPlan(OrderTradeVo order, OrderSplitRequestDto request) {
        List<SplitPlanLine> plan = new ArrayList<>();
        for (SplitReceiverDto receiver : request.getReceivers()) {
            plan.add(SplitPlanLine.builder()
                    .receiverMerchantNo(receiver.getReceiverMerchantNo())
                    .receiverAccountNo(receiver.getReceiverAccountNo())
                    .receiverAccountType(receiver.getReceiverAccountType())
                    .amount(receiver.getAmount())
                    .splitType(receiver.getSplitType() != null
                            ? receiver.getSplitType() : SplitReceiverTypeEnum.PARTNER.getCode())
                    .remark(receiver.getRemark())
                    .build());
        }
        BigDecimal totalSplit = plan.stream().map(SplitPlanLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalSplit.compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException("分账总额不能超过订单金额");
        }
        return plan;
    }

    private MerchantAccountBindVo resolveSettleAccount(String merchantNo, MerchantAccountBindVo cashAccount) throws Exception {
        try {
            return resolveAccount(merchantNo, BusiEnum.SETTLE.getCode());
        } catch (BusinessException e) {
            MerchantAccountBindVo fallback = new MerchantAccountBindVo();
            fallback.setMerchantNo(merchantNo);
            fallback.setAccountNo(cashAccount.getAccountNo());
            fallback.setAccountType(BusiEnum.SETTLE.getCode());
            return fallback;
        }
    }

    private MerchantAccountBindVo resolveAccount(String merchantNo, String accountType) throws Exception {
        Result<List<MerchantAccountBindVo>> result = merchantFeginClient.listByMerchant(merchantNo, accountType);
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()
                || CollectionUtils.isEmpty(result.getData())) {
            throw new BusinessException("商户未绑定账户类型: " + accountType);
        }
        return result.getData().get(0);
    }
}
