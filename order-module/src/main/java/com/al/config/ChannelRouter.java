package com.al.config;

import com.al.bean.business.TradeChannelEnum;
import com.al.bean.dto.account.MerchantChannelConfigDto;
import com.al.bean.dto.merchant.MerchantChannelConfigVo;
import com.al.common.Result;
import com.al.config.factory.ChannelFactory;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.service.channel.TradeChannel;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelRouter {

    private final ChannelWeightManager weightManager;

    private final ChannelFactory channelFactory;
    @Autowired
    private MerchantFeginClient merchantFeginClient;


    public TradeChannel route(String merchantNo,String payType) throws Exception {
        MerchantChannelConfigDto merchantChannelConfigDto=new MerchantChannelConfigDto();
        merchantChannelConfigDto.setMerchantNo(merchantNo);
        Result<List<MerchantChannelConfigVo>> listResult = merchantFeginClient.listConfig(merchantChannelConfigDto);
        log.info("merchant channel config infomation:{}",listResult);
        if (!CollectionUtils.isEmpty(listResult.getData())) {
            return channelFactory.route(String.valueOf(TradeChannelEnum.WX_PAY.getCode()));
        }
        MerchantChannelConfigVo config =
                weightManager.choose(listResult.getData());
        return channelFactory.get(config.getChannelCode());
    }
}
