package com.al.config.factory;


import com.al.service.channel.TradeChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChannelFactory {
    @Autowired
    private Map<String, TradeChannel> channelMap ;
    public TradeChannel route(String channel) {
        for (Map.Entry<String, TradeChannel> entry : channelMap.entrySet()) {
            TradeChannel value = entry.getValue();
            if (value.supported().getCode().equals(channel)) {
                return value;
            }

        }
        return null;
    }
}
