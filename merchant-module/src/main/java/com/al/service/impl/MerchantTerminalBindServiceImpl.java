package com.al.service.impl;

import com.al.bean.vo.MerchantTerminalBindVo;
import com.al.service.MerchantTerminalBindService;

import com.al.mapper.MerchantTerminalBindMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantTerminalBindServiceImpl implements MerchantTerminalBindService {

    private final MerchantTerminalBindMapper mapper;

    public MerchantTerminalBindServiceImpl(MerchantTerminalBindMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTerminal(MerchantTerminalBindVo bind) throws Exception {
        // 校验是否已存在绑定
        QueryWrapper<MerchantTerminalBindVo> query = new QueryWrapper<>();
        query.eq("merchant_no", bind.getMerchantNo())
                .eq("terminal_no", bind.getTerminalNo())
                .eq("status", 1);
        MerchantTerminalBindVo existing = mapper.selectOne(query);
        if (existing != null) {
            throw new Exception("终端已绑定该商户");
        }
        bind.setStatus(1);
        bind.setBindTime(LocalDateTime.now());
        bind.setCreateTime(LocalDateTime.now());
        bind.setUpdateTime(LocalDateTime.now());
        mapper.insert(bind);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindTerminal(String merchantNo, String terminalNo, String updateUser) {
        QueryWrapper<MerchantTerminalBindVo> query = new QueryWrapper<>();
        query.eq("merchant_no", merchantNo)
                .eq("terminal_no", terminalNo)
                .eq("status", 1);

        MerchantTerminalBindVo bind = mapper.selectOne(query);
        if (bind != null) {
            bind.setStatus(0);
            bind.setUnbindTime(LocalDateTime.now());
            bind.setUpdateTime(LocalDateTime.now());
            bind.setUpdateUser(updateUser);
            mapper.updateById(bind);
        }
    }

    @Override
    public List<MerchantTerminalBindVo> getTerminalsByMerchant(String merchantNo) {
        return mapper.selectList(new QueryWrapper<MerchantTerminalBindVo>()
                .eq("merchant_no", merchantNo)
                .eq("status", 1));
    }

    @Override
    public MerchantTerminalBindVo getMerchantByTerminal(String terminalNo) {
        return mapper.selectOne(new QueryWrapper<MerchantTerminalBindVo>()
                .eq("terminal_no", terminalNo)
                .eq("status", 1));
    }
}
