package com.al.service.impl;

import com.al.bean.dto.account.AccountDto;
import com.al.bean.vo.MerchantAccountBindVo;
import com.al.bean.vo.account.AccountOpenVo;
import com.al.bean.vo.account.AccountVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.fegin.account.AccountFeginClient;
import com.al.mapper.MerchantAccountBindMapper;
import com.al.service.MerchantAccountBindService;
import com.al.service.MerchantService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_INACTIVE = 0;
    private static final String DEFAULT_CHANNEL_CODE = "1";

    private final MerchantAccountBindMapper mapper;
    @Autowired
    private AccountFeginClient accountFeginClient;
    @Autowired
    private MerchantService merchantService;

    public MerchantAccountBindServiceImpl(MerchantAccountBindMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantAccountBindVo bindAccount(MerchantAccountBindVo bind) throws Exception {
        validateMerchant(bind.getMerchantNo());
        if (findActiveBind(bind.getMerchantNo(), bind.getAccountNo()) != null) {
            throw new BusinessException("账户已绑定该商户");
        }
        ensureAccountExists(bind);
        fillBindDefaults(bind);
        mapper.insert(bind);
        return bind;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAccount(String merchantNo, String accountNo, String updateUser) {
        MerchantAccountBindVo bind = findActiveBind(merchantNo, accountNo);
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
    public List<MerchantAccountBindVo> getAccountsByMerchant(String merchantNo, String acctType) {
        log.info("query merchant bind account information merchantNo:{}, acctType:{}", merchantNo, acctType);
        return mapper.selectList(Wrappers.lambdaQuery(MerchantAccountBindVo.class)
                .eq(MerchantAccountBindVo::getMerchantNo, merchantNo)
                .eq(StringUtils.isNotBlank(acctType), MerchantAccountBindVo::getAccountType, acctType)
                .eq(MerchantAccountBindVo::getStatus, STATUS_ACTIVE));
    }

    @Override
    public List<MerchantAccountBindVo> getMerchantsByAccount(String accountNo) {
        return mapper.selectList(Wrappers.lambdaQuery(MerchantAccountBindVo.class)
                .eq(MerchantAccountBindVo::getAccountNo, accountNo)
                .eq(MerchantAccountBindVo::getStatus, STATUS_ACTIVE));
    }

    private void validateMerchant(String merchantNo) throws Exception {
        if (merchantService.query(merchantNo) == null) {
            throw new BusinessException("商户不存在");
        }
    }

    private MerchantAccountBindVo findActiveBind(String merchantNo, String accountNo) {
        return mapper.selectOne(Wrappers.lambdaQuery(MerchantAccountBindVo.class)
                .eq(MerchantAccountBindVo::getMerchantNo, merchantNo)
                .eq(MerchantAccountBindVo::getAccountNo, accountNo)
                .eq(MerchantAccountBindVo::getStatus, STATUS_ACTIVE));
    }

    private void ensureAccountExists(MerchantAccountBindVo bind) throws Exception {
        AccountDto accountDto = toAccountDto(bind);
        Result<List<AccountVo>> queryResult = accountFeginClient.queryAccount(accountDto);
        if (isFeignSuccess(queryResult) && !CollectionUtils.isEmpty(queryResult.getData())) {
            return;
        }
        accountDto.setFlow(TraceUtil.createTraceId());
        accountDto.setModifyUser(bind.getCreateUser());
        accountDto.setRemark(bind.getRemark());
        log.info("bind account open request:{}", accountDto);
        Result<AccountOpenVo> openResult = accountFeginClient.openAccount(accountDto);
        assertFeignSuccess(openResult, "开户失败");
    }

    private AccountDto toAccountDto(MerchantAccountBindVo bind) {
        AccountDto dto = new AccountDto();
        dto.setMerchantNo(bind.getMerchantNo());
        dto.setAccountNo(bind.getAccountNo());
        dto.setAccountType(bind.getAccountType());
        dto.setChannelCode(resolveChannelCode(bind.getChannelCode()));
        dto.setChannelAccountNo(bind.getAccountNo());
        return dto;
    }

    private String resolveChannelCode(String channelCode) {
        return channelCode != null ? channelCode : DEFAULT_CHANNEL_CODE;
    }

    private void fillBindDefaults(MerchantAccountBindVo bind) {
        bind.setStatus(STATUS_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        bind.setBindTime(now);
        bind.setCreateTime(now);
        bind.setUpdateTime(now);
    }

    private boolean isFeignSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultEnum.SUCESS.getCode();
    }

    private void assertFeignSuccess(Result<?> result, String defaultMsg) {
        if (!isFeignSuccess(result)) {
            throw new BusinessException(result != null ? result.getMsg() : defaultMsg);
        }
    }
}
