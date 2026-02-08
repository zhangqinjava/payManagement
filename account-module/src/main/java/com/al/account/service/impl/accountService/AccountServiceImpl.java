package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.account.service.accountService.AccountService;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;
    @Autowired
    private AccountOpenMapper accountOpenMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Override
    public String save(AccountDto accountDto) throws Exception{
        try {
            log.info("start open account infomation save:{}", accountDto);
            checkParam(accountDto);
            Long count = accountOpenMapper.selectCount(Wrappers.lambdaQuery(AccountOpenFlowVo.class)
                    .eq(AccountOpenFlowVo::getOpenOrderNo, accountDto.getFlow()));
            if (count > 0) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "请求流水号重复");
            }
            String traceId = TraceUtil.createTraceId();
            accountDto.setAccountNo(traceId);
            accountTransactionImpl.save(accountDto);
            return "开户成功";
        }catch (Exception e){
            log.error("open save account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(AccountDto accountDto) {
        try {
            checkParam(accountDto);
            log.info("start open account information update:{}", accountDto);
            AccountVo build = AccountVo.builder()
                    .accountStatus(accountDto.getAccountStatus())
                    .storeId(accountDto.getStoreId())
                    .accountNo(accountDto.getAccountNo()).build();
            accountMapper.update(build, Wrappers.lambdaUpdate(AccountVo.class)
                    .eq(AccountVo::getAccountNo,accountDto.getAccountNo())
            .eq(AccountVo::getStoreId,accountDto.getStoreId())
            .eq(AccountVo::getChannelCode,accountDto.getChannelCode()));
        }catch (Exception e){
            log.error("open update account information error:{}", e.getMessage());
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(AccountDto accountDto) {
        return null;
    }

    @Override
    public List<AccountVo> queryByStoreId(AccountDto accountDto) {
        return Collections.emptyList();
    }
    public void checkParam(AccountDto accountDto) throws Exception{
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountType()) &&!BusiEnum.contains(accountDto.getAccountType())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户类型不正确");
        }
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountStatus()) && !BusiEnum.contains(accountDto.getAccountStatus())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户状态不正确");
        }
    }
}
