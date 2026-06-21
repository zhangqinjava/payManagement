package com.al.service.order;

import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.billing.BillingSplitCalculateVo;

public interface OrderFeeService {
    BillingSplitCalculateVo calculateUpfrontFee(OrderTradeVo order);

    void saveFeeSnapshot(OrderTradeVo order, BillingSplitCalculateVo feeResult, String feeFlow);
}
