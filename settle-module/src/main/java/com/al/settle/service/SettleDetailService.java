package com.al.settle.service;

import com.al.bean.vo.account.AccountQueryDtlVo;
import com.al.settle.entity.SettleDetailVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SettleDetailService extends IService<SettleDetailVo> {
    void saveDetails(String settleNo, List<AccountQueryDtlVo> details);
}
