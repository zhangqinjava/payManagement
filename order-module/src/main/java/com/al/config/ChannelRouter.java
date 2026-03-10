package com.al.config;

import com.al.bean.vo.ChannelConfigVo;
import com.al.config.factory.ChannelFactory;
import com.al.mapper.ChannelConfigMapper;
import com.al.service.channel.TradeChannel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelRouter {

    private final ChannelWeightManager weightManager;

    private final ChannelFactory channelFactory;

    private final ChannelConfigMapper channelConfigMapper;

    public TradeChannel route(String merchantNo) {

        List<ChannelConfigVo> configs =
                channelConfigMapper.selectList(Wrappers.<ChannelConfigVo>lambdaQuery().eq(ChannelConfigVo::getMerchantNo, merchantNo));
        ChannelConfigVo config =
                weightManager.choose(configs);
        return channelFactory.get(config.getChannelCode());
    }
}
