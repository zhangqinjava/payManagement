package com.al.split.service.impl;

import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.account.AccountTransferDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.account.AccountTransferVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.AccountTradeEnum;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.fegin.account.AccountFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.bean.vo.OrderSplitDetailVo;
import com.al.split.bean.vo.OrderSplitRecordVo;
import com.al.split.bean.vo.OrderSplitResultVo;
import com.al.split.enums.SplitStatusEnum;
import com.al.split.mapper.OrderSplitDetailMapper;
import com.al.split.mapper.OrderSplitRecordMapper;
import com.al.split.model.SplitPlanLine;
import com.al.split.service.OrderSplitService;
import com.al.split.service.SplitRuleService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OrderSplitServiceImpl implements OrderSplitService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");
    private static final int DETAIL_SUCCESS = 2;
    private static final int DETAIL_FAIL = 3;

    @Autowired
    private OrderSplitRecordMapper splitRecordMapper;
    @Autowired
    private OrderSplitDetailMapper splitDetailMapper;
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private SplitRuleService splitRuleService;
    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSplitResultVo executeSplit(OrderSplitRequestDto request) throws Exception {
        OrderTradeVo order = loadOrder(request.getOrderNo(), request.getMerchantNo());
        validateOrderForSplit(order);

        OrderSplitRecordVo exist = splitRecordMapper.selectOne(Wrappers.lambdaQuery(OrderSplitRecordVo.class)
                .eq(OrderSplitRecordVo::getOrderNo, order.getOrderNo())
                .eq(OrderSplitRecordVo::getMerchantNo, order.getMerchantNo()));
        if (exist != null && SplitStatusEnum.SUCCESS.getCode().equals(exist.getSplitStatus())) {
            return toResult(exist, loadDetails(exist.getSplitNo()));
        }
        if (exist != null) {
            splitDetailMapper.delete(Wrappers.lambdaQuery(OrderSplitDetailVo.class)
                    .eq(OrderSplitDetailVo::getSplitNo, exist.getSplitNo()));
            splitRecordMapper.deleteById(exist.getId());
        }

        BigDecimal feeAmount;
        BigDecimal netAmount;
        if (order.getFeeAmount() != null && AccountTradeEnum.SUCESS.getCode().equals(order.getFeeStatus())) {
            feeAmount = order.getFeeAmount();
            netAmount = order.getNetAmount() != null
                    ? order.getNetAmount() : order.getPayAmount().subtract(feeAmount);
        } else {
            feeAmount = splitRuleService.calculateFee(order);
            netAmount = order.getPayAmount().subtract(feeAmount);
        }

        List<SplitPlanLine> plan;
        if (CollectionUtils.isEmpty(request.getReceivers())) {
            if (order.getFeeAmount() != null && AccountTradeEnum.SUCESS.getCode().equals(order.getFeeStatus())) {
                plan = splitRuleService.buildPlanAfterUpfrontFee(order, netAmount);
            } else {
                plan = splitRuleService.buildDefaultPlan(order, feeAmount);
            }
        } else {
            plan = splitRuleService.buildCustomPlan(order, request);
        }
        if (plan.isEmpty()) {
            if (AccountTradeEnum.SUCESS.getCode().equals(order.getFeeStatus())) {
                return saveEmptySplitSuccess(order, feeAmount, netAmount);
            }
            throw new BusinessException("分账计划为空");
        }

        String splitNo = TraceUtil.createTraceId();
        OrderSplitRecordVo record = OrderSplitRecordVo.builder()
                .splitNo(splitNo)
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .merchantNo(order.getMerchantNo())
                .totalAmount(order.getPayAmount())
                .feeAmount(feeAmount)
                .netAmount(netAmount.max(BigDecimal.ZERO))
                .splitStatus(SplitStatusEnum.PROCESSING.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        splitRecordMapper.insert(record);

        MerchantAccountBindVo cashAccount = resolveCashAccount(order.getMerchantNo());
        List<OrderSplitDetailVo> details = new ArrayList<>();
        int successCount = 0;
        for (SplitPlanLine line : plan) {
            OrderSplitDetailVo detail = executeSplitLine(order, cashAccount, splitNo, line);
            splitDetailMapper.insert(detail);
            details.add(detail);
            if (DETAIL_SUCCESS == detail.getStatus()) {
                successCount++;
            }
        }

        int finalStatus;
        String failReason = null;
        if (successCount == plan.size()) {
            finalStatus = SplitStatusEnum.SUCCESS.getCode();
        } else if (successCount == 0) {
            finalStatus = SplitStatusEnum.FAIL.getCode();
            failReason = "全部分账明细失败";
        } else {
            finalStatus = SplitStatusEnum.PARTIAL_FAIL.getCode();
            failReason = "部分分账明细失败";
        }
        record.setSplitStatus(finalStatus);
        record.setFailReason(failReason);
        record.setUpdateTime(LocalDateTime.now());
        splitRecordMapper.updateById(record);

        log.info("订单分账完成 orderNo={}, splitNo={}, status={}", order.getOrderNo(), splitNo, finalStatus);
        return toResult(record, details);
    }

    @Override
    public OrderSplitResultVo queryByOrderNo(String orderNo, String merchantNo) {
        OrderSplitRecordVo record = splitRecordMapper.selectOne(Wrappers.lambdaQuery(OrderSplitRecordVo.class)
                .eq(OrderSplitRecordVo::getOrderNo, orderNo)
                .eq(OrderSplitRecordVo::getMerchantNo, merchantNo));
        if (record == null) {
            throw new BusinessException("分账记录不存在");
        }
        return toResult(record, loadDetails(record.getSplitNo()));
    }

    private OrderSplitDetailVo executeSplitLine(OrderTradeVo order, MerchantAccountBindVo cashAccount,
                                                String splitNo, SplitPlanLine line) {
        String flowNo = TraceUtil.createTraceId();
        String detailNo = TraceUtil.createTraceId();
        LocalDateTime now = LocalDateTime.now();
        OrderSplitDetailVo detail = OrderSplitDetailVo.builder()
                .splitNo(splitNo)
                .detailNo(detailNo)
                .receiverMerchantNo(line.getReceiverMerchantNo())
                .receiverAccountNo(line.getReceiverAccountNo())
                .receiverAccountType(line.getReceiverAccountType())
                .splitType(line.getSplitType())
                .amount(line.getAmount())
                .flowNo(flowNo)
                .status(SplitStatusEnum.PROCESSING.getCode())
                .createTime(now)
                .updateTime(now)
                .build();
        try {
            AccountTransferDto transferDto = buildTransferDto(order, cashAccount, line, flowNo);
            Result<AccountTransferVo> result = accountFeginClient.transfer(transferDto);
            if (result != null && result.getCode() == ResultEnum.SUCESS.getCode()) {
                detail.setStatus(DETAIL_SUCCESS);
            } else {
                detail.setStatus(DETAIL_FAIL);
                detail.setFailReason(result != null ? result.getMsg() : "账务转账失败");
            }
        } catch (Exception e) {
            detail.setStatus(DETAIL_FAIL);
            detail.setFailReason(e.getMessage());
            log.error("分账明细执行失败 splitNo={}, detailNo={}", splitNo, detailNo, e);
        }
        detail.setUpdateTime(LocalDateTime.now());
        return detail;
    }

    private AccountTransferDto buildTransferDto(OrderTradeVo order, MerchantAccountBindVo cashAccount,
                                                SplitPlanLine line, String flowNo) {
        LocalDateTime orderTime = order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now();
        AccountTransferDto dto = new AccountTransferDto();
        dto.setFlowNo(flowNo);
        dto.setOutAccountNo(cashAccount.getAccountNo().trim());
        dto.setOutMerchantNo(order.getMerchantNo());
        dto.setOutAccountType(cashAccount.getAccountType());
        dto.setInAccountNo(line.getReceiverAccountNo().trim());
        dto.setInMerchantNo(line.getReceiverMerchantNo());
        dto.setInAccountType(line.getReceiverAccountType());
        dto.setChannelCode(order.getPayChannel());
        dto.setBizType(order.getBizType());
        dto.setBizOrderNo(order.getOrderNo());
        dto.setBizOrderDate(orderTime.format(DATE_FMT));
        dto.setBizOrderTime(orderTime.format(TIME_FMT));
        dto.setAmount(line.getAmount().toPlainString());
        dto.setFunCode(BusiEnum.FUNCODE_TRANSFER.getCode());
        dto.setRemark(line.getRemark());
        return dto;
    }

    private OrderTradeVo loadOrder(String orderNo, String merchantNo) {
        OrderTradeVo order = orderTradeMapper.selectOne(Wrappers.lambdaQuery(OrderTradeVo.class)
                .eq(OrderTradeVo::getOrderNo, orderNo)
                .eq(OrderTradeVo::getMerchantNo, merchantNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void validateOrderForSplit(OrderTradeVo order) {
        if (!TradeStatusEnum.SUCCESS.getCode().equals(order.getTradeStatus())) {
            throw new BusinessException("仅支付成功订单可分账");
        }
        if (!AccountTradeEnum.SUCESS.getCode().equals(order.getAccountStatus())) {
            throw new BusinessException("订单尚未上账成功，不能分账");
        }
    }

    private MerchantAccountBindVo resolveCashAccount(String merchantNo) throws Exception {
        Result<List<MerchantAccountBindVo>> result = merchantFeginClient.listByMerchant(
                merchantNo, BusiEnum.CASH.getCode());
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()
                || CollectionUtils.isEmpty(result.getData())) {
            throw new BusinessException("商户未绑定现金账户");
        }
        return result.getData().get(0);
    }

    private List<OrderSplitDetailVo> loadDetails(String splitNo) {
        return splitDetailMapper.selectList(Wrappers.lambdaQuery(OrderSplitDetailVo.class)
                .eq(OrderSplitDetailVo::getSplitNo, splitNo)
                .orderByAsc(OrderSplitDetailVo::getId));
    }

    private OrderSplitResultVo saveEmptySplitSuccess(OrderTradeVo order, BigDecimal feeAmount, BigDecimal netAmount) {
        String splitNo = TraceUtil.createTraceId();
        OrderSplitRecordVo record = OrderSplitRecordVo.builder()
                .splitNo(splitNo)
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .merchantNo(order.getMerchantNo())
                .totalAmount(order.getPayAmount())
                .feeAmount(feeAmount)
                .netAmount(netAmount == null ? BigDecimal.ZERO : netAmount)
                .splitStatus(SplitStatusEnum.SUCCESS.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        splitRecordMapper.insert(record);
        log.info("前项手续费已扣划，无需分账转账 orderNo={}", order.getOrderNo());
        return toResult(record, Collections.emptyList());
    }

    private OrderSplitResultVo toResult(OrderSplitRecordVo record, List<OrderSplitDetailVo> details) {
        return OrderSplitResultVo.builder()
                .splitNo(record.getSplitNo())
                .orderNo(record.getOrderNo())
                .tradeNo(record.getTradeNo())
                .merchantNo(record.getMerchantNo())
                .totalAmount(record.getTotalAmount())
                .feeAmount(record.getFeeAmount())
                .netAmount(record.getNetAmount())
                .splitStatus(record.getSplitStatus())
                .details(details)
                .build();
    }
}
