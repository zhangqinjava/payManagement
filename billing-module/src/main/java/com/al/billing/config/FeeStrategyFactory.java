package com.al.billing.config;

import com.al.billing.service.FeeCalculator;
import com.al.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeeStrategyFactory {

    @Autowired
    private Map<String, FeeCalculator> strategyMap;

    public FeeCalculator getStrategy(Integer feeMode) {
        FeeCalculator strategy = strategyMap.get(String.valueOf(feeMode));
        if (strategy == null) {
            throw new BusinessException("不支持的计费策略：" + feeMode);
        }
        return strategy;
    }
}
