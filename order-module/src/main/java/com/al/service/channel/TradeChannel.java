package com.al.service.channel;

import com.al.bean.business.TradeChannelEnum;

import java.util.Map;

public interface TradeChannel {
    TradeChannelEnum supported();

    Object pay(Map<String,Object> trade) throws Exception;
}
