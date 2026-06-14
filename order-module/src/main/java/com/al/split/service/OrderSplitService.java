package com.al.split.service;

import com.al.split.bean.dto.OrderSplitRequestDto;
import com.al.split.bean.vo.OrderSplitResultVo;

public interface OrderSplitService {
    OrderSplitResultVo executeSplit(OrderSplitRequestDto request) throws Exception;

    OrderSplitResultVo queryByOrderNo(String orderNo, String merchantNo);
}
