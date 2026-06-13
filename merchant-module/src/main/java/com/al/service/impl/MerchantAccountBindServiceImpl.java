package com.al.service.impl;

import com.al.bean.dto.account.AccountDto;
import com.al.bean.vo.MerchantAccountBindVo;
import com.al.bean.vo.account.AccountVo;
import com.al.common.Result;
import com.al.common.util.TraceUtil;
import com.al.fegin.account.AccountFeginClient;
import com.al.mapper.MerchantAccountBindMapper;
import com.al.service.MerchantAccountBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MerchantAccountBindServiceImpl implements MerchantAccountBindService {

    private final MerchantAccountBindMapper mapper;
    @Autowired
    private AccountFeginClient accountFeginClient;

    public MerchantAccountBindServiceImpl(MerchantAccountBindMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantAccountBindVo bindAccount(MerchantAccountBindVo bind) throws Exception {
        QueryWrapper<MerchantAccountBindVo> query = new QueryWrapper<>();
        query.eq("merchant_no", bind.getMerchantNo())
                .eq("account_no", bind.getAccountNo())
                .eq("status", 1);
        MerchantAccountBindVo exist = mapper.selectOne(query);
        if (exist != null) {
            throw new Exception("账户已绑定该商户");
        }

        ensureAccountExists(bind);

        bind.setStatus(1);
        bind.setBindTime(LocalDateTime.now());
        bind.setCreateTime(LocalDateTime.now());
        bind.setUpdateTime(LocalDateTime.now());
        mapper.insert(bind);
        return bind;
    }

    private void ensureAccountExists(MerchantAccountBindVo bind) throws Exception {
        AccountDto queryDto = new AccountDto();
        queryDto.setFlow(TraceUtil.createTraceId());
        queryDto.setMerchantNo(bind.getMerchantNo());
        queryDto.setAccountNo(bind.getAccountNo());
        queryDto.setAccountType(bind.getAccountType());
        queryDto.setChannelCode(bind.getChannelCode() != null ? bind.getChannelCode() : "1");
        queryDto.setChannelAccountNo(bind.getAccountNo());
        Result<List<AccountVo>> queryResult = accountFeginClient.queryAccount(queryDto);
        if (queryResult != null && !CollectionUtils.isEmpty(queryResult.getData())) {
            return;
        }
        AccountDto openDto = new AccountDto();
        openDto.setFlow(TraceUtil.createTraceId());
        openDto.setMerchantNo(bind.getMerchantNo());
        openDto.setAccountNo(bind.getAccountNo());
        openDto.setAccountType(bind.getAccountType());
        openDto.setChannelCode(bind.getChannelCode() != null ? bind.getChannelCode() : "1");
        openDto.setChannelAccountNo(bind.getAccountNo());
        openDto.setModifyUser(bind.getCreateUser());
        openDto.setRemark(bind.getRemark());
        log.info("bind account open request:{}", openDto);
        accountFeginClient.openAccount(openDto);
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
    public List<MerchantAccountBindVo> getAccountsByMerchant(String merchantNo, String acctType) {
        log.info("query metchantNo bind account information merchantNo:{} ,acctType:{}", merchantNo, acctType);
        return mapper.selectList(new QueryWrapper<MerchantAccountBindVo>()
                .eq("merchant_no", merchantNo)
                .eq("account_type", acctType)
                .eq("status", 1));
    }

    @Override
    public List<MerchantAccountBindVo> getMerchantsByAccount(String accountNo) {
        return mapper.selectList(new QueryWrapper<MerchantAccountBindVo>()
                .eq("account_no", accountNo)
                .eq("status", 1));
    }
}
