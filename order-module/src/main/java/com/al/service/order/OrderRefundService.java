package com.al.service.order;

import com.al.bean.dto.OrderRefundTradeDto;
import com.al.bean.vo.OrderrefundTradeVo;

public interface OrderRefundService {
    OrderrefundTradeVo createRefundOrder(OrderRefundTradeDto orderRefundTradeDto) throws Exception;
}
