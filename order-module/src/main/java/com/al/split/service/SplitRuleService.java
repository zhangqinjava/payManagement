package com.al.split.service;

import com.al.bean.vo.OrderTradeVo;
import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.bean.vo.OrderSplitResultVo;
import com.al.split.model.SplitPlanLine;

import java.math.BigDecimal;
import java.util.List;

public interface SplitRuleService {
    BigDecimal calculateFee(OrderTradeVo order) throws Exception;

    List<SplitPlanLine> buildDefaultPlan(OrderTradeVo order, BigDecimal feeAmount) throws Exception;

    List<SplitPlanLine> buildCustomPlan(OrderTradeVo order, OrderSplitRequestDto request);
}
