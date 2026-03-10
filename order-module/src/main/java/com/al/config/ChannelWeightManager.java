package com.al.config;

import com.al.bean.vo.ChannelConfigVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ChannelWeightManager {

    public ChannelConfigVo choose(List<ChannelConfigVo> configs) {
        int totalWeight = configs.stream()
                .mapToInt(ChannelConfigVo::getWeight)
                .sum();
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (ChannelConfigVo config : configs) {
            current += config.getWeight();
            if (random < current) {
                return config;
            }
        }
        return null;
    }
}
