package com.al.service.impl.order;

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
import com.al.service.order.OrderTradeService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
        try {
            log.info("开始创建退款订单，请求参数: {}", orderRefundTradeDto);

            // 1. 验证参数
            validateRefundRequest(orderRefundTradeDto);

            // 2. 查询原订单
            OrderTradeVo originalOrder = queryOriginalOrder(orderRefundTradeDto.getOrderNo(), orderRefundTradeDto.getMerchantNo());

            // 3. 验证订单状态和退款金额
            validateOrderForRefund(originalOrder, orderRefundTradeDto.getRefundAmount());

            // 4. 生成退款单号
            String refundNo = TraceUtil.createTraceId();

            // 5. 创建退款记录
            OrderrefundTradeVo refundRecord = createRefundRecord(refundNo, orderRefundTradeDto, originalOrder);

            // 6. 保存退款记录到数据库
            this.save(refundRecord);

            // 7. 调用账户下账操作（从商户账户扣除退款金额）
            deductFromAccount(orderRefundTradeDto, originalOrder, refundNo);

            // 8. 调用支付渠道退款接口
            callChannelRefund(refundRecord, originalOrder);

            // 9. 更新退款状态为处理中（如果渠道同步返回成功，则更新为成功）
            refundRecord.setStatus(1); // 1表示退款处理中
            refundRecord.setUpdateTime(LocalDateTime.now());
            this.updateById(refundRecord);

            log.info("退款订单创建成功，退款单号: {}", refundNo);
            return refundRecord;

        } catch (Exception e) {
            log.error("创建退款订单失败: {}", e.getMessage(), e);
            throw e;
        }
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
        // 检查订单状态是否为成功
        if (!TradeStatusEnum.SUCCESS.getCode().equals(order.getTradeStatus()) ) {
            throw new BusinessException("只有成功的订单才能退款");
        }
        List<OrderrefundTradeVo> list = this.list(Wrappers.<OrderrefundTradeVo>lambdaQuery().eq(OrderrefundTradeVo::getOrderNo, order.getOrderNo()));
        BigDecimal sumRefundCount = CollectionUtils.isEmpty(list) ? BigDecimal.ZERO
                : list.stream().map(OrderrefundTradeVo::getRefundAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (refundAmount.add(sumRefundCount).compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException("退款金额不能超过订单金额");
        }
        // TODO: 可以添加检查是否已有退款记录，避免重复退款
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
                .status(0) // 0表示初始状态
                .notifyStatus("0") // 0表示未通知
                .refundTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private void deductFromAccount(OrderRefundTradeDto dto, OrderTradeVo originalOrder, String refundNo) throws Exception {
        Result<List<MerchantAccountBindVo>> bindResult = merchantFeginClient.listByMerchant(
                dto.getMerchantNo(), BusiEnum.CASH.getCode());
        if (bindResult == null || CollectionUtils.isEmpty(bindResult.getData())) {
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

        if (result == null || !String.valueOf(ResultEnum.SUCESS.getCode()).equals(result.getCode())) {
            throw new BusinessException("账户下账失败: " + (result != null ? result.getMsg() : "未知错误"));
        }

        log.info("账户下账成功，退款单号: {}", refundNo);
    }

    private void callChannelRefund(OrderrefundTradeVo refundRecord, OrderTradeVo originalOrder) throws Exception {
        try {
            // 构建退款请求参数
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("refundNo", refundRecord.getRefundNo());
            refundParams.put("orderNo", refundRecord.getOrderNo());
            refundParams.put("merchantNo", refundRecord.getMerchantNo());
            refundParams.put("refundAmount", refundRecord.getRefundAmount());
            refundParams.put("channelTrace", originalOrder.getChannelTrace());
            refundParams.put("payChannel", originalOrder.getPayChannel());

            // 获取支付渠道
            TradeChannel channel = channelRouter.route(refundRecord.getMerchantNo(),originalOrder.getPayChannel());

            // 调用渠道退款接口
            // 注意：TradeChannel接口目前只有pay方法，需要扩展refund方法
            // 暂时先记录日志，后续扩展
            log.info("调用支付渠道退款，参数: {}", refundParams);

            // TODO: 扩展TradeChannel接口支持refund方法
            // Object result = channel.refund(refundParams);

        } catch (Exception e) {
            log.error("调用支付渠道退款失败: {}", e.getMessage(), e);
            // 不抛出异常，允许退款记录创建，后续异步处理
        }
    }
}
