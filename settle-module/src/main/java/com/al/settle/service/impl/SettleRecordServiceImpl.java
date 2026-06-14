package com.al.settle.service.impl;

import com.al.settle.entity.SettleRecordVo;
import com.al.settle.mapper.SettleRecordMapper;
import com.al.settle.service.SettleRecordService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SettleRecordServiceImpl extends ServiceImpl<SettleRecordMapper, SettleRecordVo> implements SettleRecordService {

    @Override
    public boolean existsRecord(String merchantNo, String busiType, LocalDate startDate, LocalDate endDate) {
        return count(Wrappers.lambdaQuery(SettleRecordVo.class)
                .eq(SettleRecordVo::getMerchantNo, merchantNo)
                .eq(SettleRecordVo::getBusiType, busiType)
                .eq(SettleRecordVo::getSettleStartDate, startDate)
                .eq(SettleRecordVo::getSettleEndDate, endDate)) > 0;
    }
}
