package com.al.service.order;

import com.al.bean.dto.OrderQueryDto;
import com.al.bean.dto.OrderRefundTradeDto;
import com.al.bean.dto.OrderTradeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.OrderrefundTradeVo;

import java.util.List;

public interface OrderService {
    OrderTradeVo create(OrderTradeDto orderTradeDto) throws Exception;
    List<OrderTradeVo> query(OrderQueryDto orderTradeDto) throws Exception;
    OrderTradeVo queryByOrderNo(String orderNo) throws Exception;
}
