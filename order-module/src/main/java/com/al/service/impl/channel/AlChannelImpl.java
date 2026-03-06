package com.al.service.impl.channel;

import com.al.bean.business.TradeChannelEnum;
import com.al.service.channel.TradeChannel;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("ALI_PAY")
public class AlChannelImpl implements TradeChannel {
    @Override
    public TradeChannelEnum supported() {
        return TradeChannelEnum.ALI_PAY ;
    }

    @Override
    public Object pay(Map<String,Object> map) {
        return null;
    }
}
