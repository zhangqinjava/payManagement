package com.al.config;

import com.al.service.FeeCalculator;
import com.al.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeeStrategyFactory {

    @Autowired
    private Map<String, FeeCalculator> strategyMap;

    public FeeCalculator getStrategy(String strategyCode) {
        FeeCalculator strategy = strategyMap.get(strategyCode);
        if (strategy == null) {
            throw new BusinessException("不支持的计费策略：" + strategyCode);
        }
        return strategy;
    }
}
