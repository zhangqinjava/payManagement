package com.al.service.impl.order;

import com.al.bean.business.TradeStatusEnum;
import com.al.bean.dto.OrderQueryDto;
import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.merchant.MerchantVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.Const;
import com.al.common.business.MerchantEnum;
import com.al.common.exception.BusinessException;
import com.al.config.ChannelRouter;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.channel.TradeChannel;
import com.al.service.order.OrderService;
import com.al.service.order.OrderTradeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderTradeMapper, OrderTradeVo> implements OrderService {

    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private OrderTradeService orderTradeService;
    @Autowired
    private ChannelRouter channelRouter;

    @Override
    public OrderTradeVo create(OrderTradeDto orderTradeDto) throws Exception {
        RLock lock = redissonClient.getLock(Const.ORDER_LOCK + orderTradeDto.getOrderNo());
        boolean locked = false;
        try {
            log.info("order create event start request param:{}", orderTradeDto);
            locked = lock.tryLock();
            if (!locked) {
                throw new BusinessException("订单处理中，请勿重复提交");
            }
            OrderTradeVo exists = orderTradeMapper.selectOne(
                    Wrappers.<OrderTradeVo>lambdaQuery()
                            .eq(OrderTradeVo::getMerchantNo, orderTradeDto.getMerchantNo())
                            .eq(OrderTradeVo::getOrderNo, orderTradeDto.getOrderNo())
            );
            if (exists != null) {
                throw new BusinessException("订单已经存在");
            }
            MerchantVo merchantVo = queryMerchant(orderTradeDto.getMerchantNo());
            if (merchantVo == null || !MerchantEnum.NOMAL.getCode().equals(merchantVo.getStatus())) {
                throw new BusinessException("商户状态不正确");
            }
            OrderTradeVo order = orderTradeService.createOrder(orderTradeDto);
            Object channelResult = invokeChannel(orderTradeDto, order);
            log.info("channel response:{}", channelResult);
            return orderTradeService.createAndPay(order, channelResult);
        } catch (Exception e) {
            log.error("order create fail message:{}", e.getMessage());
            throw e;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<OrderTradeVo> query(OrderQueryDto orderTradeDto) throws Exception {
        log.info("order query start request param:{}", orderTradeDto);
        LambdaQueryWrapper<OrderTradeVo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(OrderTradeVo::getMerchantNo, orderTradeDto.getMerchantNo());
        if (orderTradeDto.getOrderNo() != null && !orderTradeDto.getOrderNo().isEmpty()) {
            wrapper.eq(OrderTradeVo::getOrderNo, orderTradeDto.getOrderNo());
        }
        if (orderTradeDto.getBizType() != null && !orderTradeDto.getBizType().isEmpty()) {
            wrapper.eq(OrderTradeVo::getBizType, orderTradeDto.getBizType());
        }
        LocalDate startLocalDate = LocalDate.parse(orderTradeDto.getStartDate(), DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate endLocalDate = LocalDate.parse(orderTradeDto.getEndDate(), DateTimeFormatter.BASIC_ISO_DATE);
        if (startLocalDate.isAfter(endLocalDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        wrapper.between(OrderTradeVo::getOrderDate,
                startLocalDate.atStartOfDay(),
                endLocalDate.atTime(23, 59, 59));
        wrapper.orderByDesc(OrderTradeVo::getOrderDate);
        List<OrderTradeVo> orderList = orderTradeMapper.selectList(wrapper);
        log.info("order query success, result size:{}", orderList.size());
        return orderList;
    }

    @Override
    public OrderTradeVo queryByOrderNo(String orderNo) throws Exception {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new BusinessException("订单号不能为空");
        }
        OrderTradeVo order = orderTradeMapper.selectOne(Wrappers.<OrderTradeVo>lambdaQuery()
                .eq(OrderTradeVo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public String updateStatus(String orderNo) throws Exception {
        OrderTradeVo order = queryByOrderNo(orderNo);
        if (!TradeStatusEnum.PAYING.getCode().equals(order.getTradeStatus())) {
            throw new BusinessException("仅支付中订单可更新为成功");
        }
        OrderTradeVo build = OrderTradeVo.builder()
                .tradeStatus(TradeStatusEnum.SUCCESS.getCode())
                .successTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        int updated = orderTradeMapper.update(build, Wrappers.<OrderTradeVo>lambdaUpdate()
                .eq(OrderTradeVo::getOrderNo, orderNo)
                .eq(OrderTradeVo::getTradeStatus, TradeStatusEnum.PAYING.getCode()));
        if (updated == 0) {
            throw new BusinessException("订单状态更新失败");
        }
        return "更新成功";
    }

    private Object invokeChannel(OrderTradeDto orderTradeDto, OrderTradeVo order) throws Exception {
        Map<String, Object> param = buildChannelParam(orderTradeDto, order);
        TradeChannel channel = channelRouter.route(orderTradeDto.getMerchantNo(), orderTradeDto.getPayChannel());
        return channel.pay(param);
    }

    private Map<String, Object> buildChannelParam(OrderTradeDto dto, OrderTradeVo order) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderNo", dto.getOrderNo());
        param.put("merchantNo", dto.getMerchantNo());
        param.put("tradeNo", order.getTradeNo());
        param.put("out_trade_no", order.getTradeNo());
        param.put("payAmount", dto.getAmount());
        param.put("channelTrace", order.getChannelTrace());
        param.put("payChannel", dto.getPayChannel());
        param.put("terminalNo", dto.getTerminalNo());
        return param;
    }

    private MerchantVo queryMerchant(String merchantNo) throws Exception {
        Result<MerchantVo> result = merchantFeginClient.query(merchantNo);
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()) {
            throw new BusinessException(result != null ? result.getMsg() : "商户系统调用失败");
        }
        return result.getData();
    }
}
