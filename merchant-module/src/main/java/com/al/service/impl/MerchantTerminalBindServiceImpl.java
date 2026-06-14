package com.al.service.impl;

import com.al.bean.vo.MerchantTerminalBindVo;
import com.al.common.exception.BusinessException;
import com.al.mapper.MerchantTerminalBindMapper;
import com.al.service.MerchantTerminalBindService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantTerminalBindServiceImpl implements MerchantTerminalBindService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_INACTIVE = 0;

    private final MerchantTerminalBindMapper mapper;

    public MerchantTerminalBindServiceImpl(MerchantTerminalBindMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTerminal(MerchantTerminalBindVo bind) throws Exception {
        MerchantTerminalBindVo existing = findActiveBind(bind.getMerchantNo(), bind.getTerminalNo());
        if (existing != null) {
            throw new BusinessException("终端已绑定该商户");
        }
        LocalDateTime now = LocalDateTime.now();
        bind.setStatus(STATUS_ACTIVE);
        bind.setBindTime(now);
        bind.setCreateTime(now);
        bind.setUpdateTime(now);
        mapper.insert(bind);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindTerminal(String merchantNo, String terminalNo, String updateUser) {
        MerchantTerminalBindVo bind = findActiveBind(merchantNo, terminalNo);
        if (bind == null) {
            return;
        }
        bind.setStatus(STATUS_INACTIVE);
        bind.setUnbindTime(LocalDateTime.now());
        bind.setUpdateTime(LocalDateTime.now());
        bind.setUpdateUser(updateUser);
        mapper.updateById(bind);
    }

    @Override
    public List<MerchantTerminalBindVo> getTerminalsByMerchant(String merchantNo) {
        return mapper.selectList(Wrappers.lambdaQuery(MerchantTerminalBindVo.class)
                .eq(MerchantTerminalBindVo::getMerchantNo, merchantNo)
                .eq(MerchantTerminalBindVo::getStatus, STATUS_ACTIVE));
    }

    @Override
    public MerchantTerminalBindVo getMerchantByTerminal(String terminalNo) {
        return mapper.selectOne(Wrappers.lambdaQuery(MerchantTerminalBindVo.class)
                .eq(MerchantTerminalBindVo::getTerminalNo, terminalNo)
                .eq(MerchantTerminalBindVo::getStatus, STATUS_ACTIVE));
    }

    private MerchantTerminalBindVo findActiveBind(String merchantNo, String terminalNo) {
        return mapper.selectOne(Wrappers.lambdaQuery(MerchantTerminalBindVo.class)
                .eq(MerchantTerminalBindVo::getMerchantNo, merchantNo)
                .eq(MerchantTerminalBindVo::getTerminalNo, terminalNo)
                .eq(MerchantTerminalBindVo::getStatus, STATUS_ACTIVE));
    }
}
