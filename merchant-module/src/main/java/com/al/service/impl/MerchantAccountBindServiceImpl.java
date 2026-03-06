package com.al.service.impl;


import com.al.bean.vo.MerchantAccountBindVo;
import com.al.mapper.MerchantAccountBindMapper;
import com.al.service.MerchantAccountBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantAccountBindServiceImpl implements MerchantAccountBindService {

    private final MerchantAccountBindMapper mapper;

    public MerchantAccountBindServiceImpl(MerchantAccountBindMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindAccount(MerchantAccountBindVo bind) throws Exception {
        // 校验是否已绑定
        QueryWrapper<MerchantAccountBindVo> query = new QueryWrapper<>();
        query.eq("merchant_no", bind.getMerchantNo())
                .eq("account_no", bind.getAccountNo())
                .eq("status", 1);
        MerchantAccountBindVo exist = mapper.selectOne(query);
        if (exist != null) {
            throw new Exception("账户已绑定该商户");
        }

        bind.setStatus(1);
        bind.setBindTime(LocalDateTime.now());
        bind.setCreateTime(LocalDateTime.now());
        bind.setUpdateTime(LocalDateTime.now());
        mapper.insert(bind);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAccount(String merchantNo, String accountNo, String updateUser) {
        QueryWrapper<MerchantAccountBindVo> query = new QueryWrapper<>();
        query.eq("merchant_no", merchantNo)
                .eq("account_no", accountNo)
                .eq("status", 1);

        MerchantAccountBindVo bind = mapper.selectOne(query);
        if (bind != null) {
            bind.setStatus(0);
            bind.setUnbindTime(LocalDateTime.now());
            bind.setUpdateTime(LocalDateTime.now());
            bind.setUpdateUser(updateUser);
            mapper.updateById(bind);
        }
    }

    @Override
    public List<MerchantAccountBindVo> getAccountsByMerchant(String merchantNo) {
        return mapper.selectList(new QueryWrapper<MerchantAccountBindVo>()
                .eq("merchant_no", merchantNo)
                .eq("status", 1));
    }

    @Override
    public List<MerchantAccountBindVo> getMerchantsByAccount(String accountNo) {
        return mapper.selectList(new QueryWrapper<MerchantAccountBindVo>()
                .eq("account_no", accountNo)
                .eq("status", 1));
    }
}
