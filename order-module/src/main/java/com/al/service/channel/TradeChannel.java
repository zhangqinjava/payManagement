package com.al.service.channel;

import com.al.bean.business.TradeChannelEnum;

import java.util.Map;

public interface TradeChannel {
    TradeChannelEnum supported();

    Object pay(Map<String, Object> trade) throws Exception;

    default Object refund(Map<String, Object> refund) throws Exception {
        throw new UnsupportedOperationException("渠道暂不支持退款: " + supported());
    }

    default boolean healthCheck() {
        return true;
    }
}
