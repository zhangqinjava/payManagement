package com.al.service.order;

import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;

public interface OrderService {
    OrderTradeVo create(OrderTradeDto orderTradeDto) throws Exception;
}
