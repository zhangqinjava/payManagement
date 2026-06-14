package com.al.service.impl.order;

import com.al.bean.business.ChannelResultHelper;
import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.OrderRefundTradeDto;
import com.al.bean.dto.account.AccountUpDownDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.OrderrefundTradeVo;
import com.al.bean.vo.merchant.MerchantAccountBindVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.config.ChannelRouter;
import com.al.fegin.account.AccountFeginClient;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderRefundTradeMapper;
import com.al.mapper.OrderTradeMapper;
import com.al.service.channel.TradeChannel;
import com.al.service.order.OrderRefundService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundTradeMapper, OrderrefundTradeVo> implements OrderRefundService {

    private static final int REFUND_INIT = 0;
    private static final int REFUND_PROCESSING = 1;
    private static final int REFUND_SUCCESS = 2;
    private static final int REFUND_FAIL = 3;

    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Autowired
    private ChannelRouter channelRouter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderrefundTradeVo createRefundOrder(OrderRefundTradeDto orderRefundTradeDto) throws Exception {
        log.info("开始创建退款订单，请求参数: {}", orderRefundTradeDto);
        validateRefundRequest(orderRefundTradeDto);
        OrderTradeVo originalOrder = queryOriginalOrder(orderRefundTradeDto.getOrderNo(), orderRefundTradeDto.getMerchantNo());
        validateOrderForRefund(originalOrder, orderRefundTradeDto.getRefundAmount());

        String refundNo = TraceUtil.createTraceId();
        OrderrefundTradeVo refundRecord = createRefundRecord(refundNo, orderRefundTradeDto, originalOrder);
        this.save(refundRecord);

        deductFromAccount(orderRefundTradeDto, originalOrder, refundNo);
        callChannelRefund(refundRecord, originalOrder);

        refundRecord.setStatus(REFUND_PROCESSING);
        refundRecord.setUpdateTime(LocalDateTime.now());
        this.updateById(refundRecord);
        log.info("退款订单创建成功，退款单号: {}", refundNo);
        return refundRecord;
    }

    private void validateRefundRequest(OrderRefundTradeDto dto) {
        if (dto.getOrderNo() == null || dto.getOrderNo().isEmpty()) {
            throw new BusinessException("订单号不能为空");
        }
        if (dto.getMerchantNo() == null || dto.getMerchantNo().isEmpty()) {
            throw new BusinessException("商户号不能为空");
        }
        if (dto.getRefundAmount() == null || dto.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }
    }

    private OrderTradeVo queryOriginalOrder(String orderNo, String merchantNo) {
        OrderTradeVo order = orderTradeMapper.selectOne(
                Wrappers.<OrderTradeVo>lambdaQuery()
                        .eq(OrderTradeVo::getOrderNo, orderNo)
                        .eq(OrderTradeVo::getMerchantNo, merchantNo)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void validateOrderForRefund(OrderTradeVo order, BigDecimal refundAmount) {
        if (!TradeStatusEnum.SUCCESS.getCode().equals(order.getTradeStatus())) {
            throw new BusinessException("只有成功的订单才能退款");
        }
        List<OrderrefundTradeVo> list = this.list(Wrappers.<OrderrefundTradeVo>lambdaQuery()
                .eq(OrderrefundTradeVo::getOrderNo, order.getOrderNo())
                .eq(OrderrefundTradeVo::getMerchantNo, order.getMerchantNo()));
        boolean hasProcessing = list.stream().anyMatch(r ->
                r.getStatus() != null && (r.getStatus() == REFUND_INIT || r.getStatus() == REFUND_PROCESSING));
        if (hasProcessing) {
            throw new BusinessException("存在处理中的退款单，请勿重复提交");
        }
        BigDecimal sumRefundCount = CollectionUtils.isEmpty(list) ? BigDecimal.ZERO
                : list.stream()
                .filter(r -> r.getStatus() == null || r.getStatus() != REFUND_FAIL)
                .map(OrderrefundTradeVo::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (refundAmount.add(sumRefundCount).compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException("退款金额不能超过订单金额");
        }
    }

    private OrderrefundTradeVo createRefundRecord(String refundNo, OrderRefundTradeDto dto, OrderTradeVo originalOrder) {
        return OrderrefundTradeVo.builder()
                .refundNo(refundNo)
                .orderNo(dto.getOrderNo())
                .merchantNo(dto.getMerchantNo())
                .channel(originalOrder.getPayChannel())
                .channelNo(originalOrder.getChannelTrace())
                .refundAmount(dto.getRefundAmount())
                .refundReason(dto.getReason())
                .status(REFUND_INIT)
                .notifyStatus("0")
                .refundTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private void deductFromAccount(OrderRefundTradeDto dto, OrderTradeVo originalOrder, String refundNo) throws Exception {
        Result<List<MerchantAccountBindVo>> bindResult = merchantFeginClient.listByMerchant(
                dto.getMerchantNo(), BusiEnum.CASH.getCode());
        if (bindResult == null || bindResult.getCode() != ResultEnum.SUCESS.getCode()
                || CollectionUtils.isEmpty(bindResult.getData())) {
            throw new BusinessException("没有查询到商户绑定的账户信息");
        }
        MerchantAccountBindVo bind = bindResult.getData().get(0);
        LocalDateTime now = LocalDateTime.now();
        AccountUpDownDto accountDownDto = new AccountUpDownDto();
        accountDownDto.setFlowNo(refundNo);
        accountDownDto.setAccountNo(bind.getAccountNo().trim());
        accountDownDto.setAccountType(bind.getAccountType());
        accountDownDto.setMerchantNo(dto.getMerchantNo());
        accountDownDto.setChannelCode(originalOrder.getPayChannel());
        accountDownDto.setBizType(BusiEnum.BIZ_TYPE_REFUND.getCode());
        accountDownDto.setBizOrderNo(refundNo);
        accountDownDto.setBizOrderDate(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        accountDownDto.setBizOrderTime(now.format(DateTimeFormatter.ofPattern("HHmmss")));
        accountDownDto.setAmount(dto.getRefundAmount().toPlainString());
        accountDownDto.setFunCode(BusiEnum.FUNCODE_DOWNWAY.getCode());
        accountDownDto.setRemark(dto.getReason());
        Result<com.al.bean.vo.account.AccountUpDownVo> result = accountFeginClient.downway(accountDownDto);
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()) {
            throw new BusinessException("账户下账失败: " + (result != null ? result.getMsg() : "未知错误"));
        }
        log.info("账户下账成功，退款单号: {}", refundNo);
    }

    private void callChannelRefund(OrderrefundTradeVo refundRecord, OrderTradeVo originalOrder) {
        try {
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("refundNo", refundRecord.getRefundNo());
            refundParams.put("orderNo", refundRecord.getOrderNo());
            refundParams.put("merchantNo", refundRecord.getMerchantNo());
            refundParams.put("refundAmount", refundRecord.getRefundAmount());
            refundParams.put("channelTrace", originalOrder.getChannelTrace());
            refundParams.put("payChannel", originalOrder.getPayChannel());
            TradeChannel channel = channelRouter.route(refundRecord.getMerchantNo(), originalOrder.getPayChannel());
            Object result = channel.refund(refundParams);
            if (ChannelResultHelper.isRefundSuccess(result)) {
                refundRecord.setStatus(REFUND_SUCCESS);
                log.info("渠道退款成功，退款单号: {}", refundRecord.getRefundNo());
            } else {
                log.warn("渠道退款未成功，退款单号: {}，结果: {}", refundRecord.getRefundNo(), result);
            }
        } catch (UnsupportedOperationException e) {
            log.warn("渠道暂不支持退款，退款单号: {}，后续异步处理", refundRecord.getRefundNo());
        } catch (Exception e) {
            log.error("调用支付渠道退款失败: {}", e.getMessage(), e);
        }
    }
}
