package com.al.settle.service;

import com.al.settle.entity.SettleRecordVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 结算记录服务
 */
public interface SettleRecordService extends IService<SettleRecordVo> {
    boolean existsRecord(String merchantNo, String busiType, java.time.LocalDate startDate, java.time.LocalDate endDate);
}