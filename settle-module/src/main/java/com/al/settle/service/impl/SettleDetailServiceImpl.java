package com.al.settle.service.impl;

import com.al.bean.vo.account.AccountQueryDtlVo;
import com.al.settle.entity.SettleDetailVo;
import com.al.settle.mapper.SettleDetailMapper;
import com.al.settle.service.SettleDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SettleDetailServiceImpl extends ServiceImpl<SettleDetailMapper, SettleDetailVo> implements SettleDetailService {

    @Override
    public void saveDetails(String settleNo, List<AccountQueryDtlVo> details) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<SettleDetailVo> records = new ArrayList<>(details.size());
        LocalDateTime now = LocalDateTime.now();
        for (AccountQueryDtlVo detail : details) {
            records.add(SettleDetailVo.builder()
                    .settleNo(settleNo)
                    .flowNo(detail.getFlowNo())
                    .flowDtlNo(detail.getFlowDtlNo())
                    .bizOrderNo(detail.getBizOrderNo())
                    .bizType(detail.getBizType())
                    .funCode(detail.getFunCode())
                    .amount(detail.getAmount())
                    .fundDirection(detail.getFundDirection())
                    .orderDate(detail.getOrderDate())
                    .createTime(now)
                    .build());
        }
        saveBatch(records);
    }
}
