package com.al.config;

import com.al.bean.dto.merchant.MerchantChannelConfigVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ChannelWeightManager {

    public MerchantChannelConfigVo choose(List<MerchantChannelConfigVo> configs) {
        int totalWeight = configs.stream()
                .mapToInt(MerchantChannelConfigVo::getWeight)
                .sum();
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (MerchantChannelConfigVo config : configs) {
            current += config.getWeight();
            if (random < current) {
                return config;
            }
        }
        return null;
    }
}
