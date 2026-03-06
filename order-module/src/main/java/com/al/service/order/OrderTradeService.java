package com.al.service.order;

import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;

public interface OrderTradeService {

    OrderTradeVo createAndPay(OrderTradeDto request) throws Exception;

    void handleCallback(String tradeNo, String channelStatus);

    OrderTradeVo query(String tradeNo);
}
