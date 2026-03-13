package com.al.config.factory;


import com.al.common.exception.BusinessException;
import com.al.service.channel.TradeChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChannelFactory {
    @Autowired
    private Map<String, TradeChannel> channelMap ;
    public TradeChannel route(String channel) {
        log.info("channel info :{}route:{}",channel,channelMap);
        if (channelMap.containsKey(channel)) {
            return channelMap.get(channel);
        }
        throw new BusinessException("not support channel type");
    }
    public TradeChannel get(String code) {
        return channelMap.get(code);
    }
}
