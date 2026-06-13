package com.al.service.impl;

import com.al.bean.dto.MerchantOnboardDto;
import com.al.bean.vo.MerchantAccountBindVo;
import com.al.bean.vo.MerchantOnboardVo;
import com.al.bean.vo.MerchantVo;
import com.al.common.business.BusiEnum;
import com.al.common.util.TraceUtil;
import com.al.service.MerchantAccountBindService;
import com.al.service.MerchantOnboardService;
import com.al.service.MerchantService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MerchantOnboardServiceImpl implements MerchantOnboardService {

    private static final String DEFAULT_CHANNEL_CODE = "1";

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MerchantAccountBindService accountBindService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantOnboardVo onboard(MerchantOnboardDto dto) throws Exception {
        log.info("merchant onboard start:{}", dto);
        MerchantVo merchant = merchantService.save(dto.getMerchant());
        String accountNo = StringUtils.isNotBlank(dto.getAccountNo())
                ? dto.getAccountNo().trim() : TraceUtil.createTraceId();
        MerchantAccountBindVo accountBind = accountBindService.bindAccount(
                toBindVo(dto, merchant.getMerchantNo(), accountNo));
        return MerchantOnboardVo.builder()
                .merchant(merchant)
                .accountBind(accountBind)
                .build();
    }

    private MerchantAccountBindVo toBindVo(MerchantOnboardDto dto, String merchantNo, String accountNo) {
        MerchantAccountBindVo bind = new MerchantAccountBindVo();
        bind.setMerchantNo(merchantNo);
        bind.setAccountNo(accountNo);
        bind.setAccountType(StringUtils.isNotBlank(dto.getAccountType())
                ? dto.getAccountType() : BusiEnum.CASH.getCode());
        bind.setChannelCode(StringUtils.isNotBlank(dto.getChannelCode())
                ? dto.getChannelCode() : DEFAULT_CHANNEL_CODE);
        bind.setCreateUser(StringUtils.isNotBlank(dto.getCreateUser())
                ? dto.getCreateUser() : "system");
        bind.setRemark(dto.getRemark());
        return bind;
    }
}
