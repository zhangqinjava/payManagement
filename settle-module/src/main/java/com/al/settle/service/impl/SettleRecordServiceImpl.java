package com.al.settle.service.impl;

import com.al.settle.entity.SettleRecordVo;
import com.al.settle.mapper.SettleRecordMapper;
import com.al.settle.service.SettleRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 结算记录服务实现
 */
@Service
public class SettleRecordServiceImpl extends ServiceImpl<SettleRecordMapper, SettleRecordVo> implements SettleRecordService {
}