package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountUpDto;
import com.al.account.bean.vo.AccountUpDownVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountMapper;
import com.al.account.service.accountService.AccountBanlanceService;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 账户余额操作
 */
@Service
@Slf4j
public class AccountBanlanceServiceImpl implements AccountBanlanceService {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;
    @Override
    public AccountUpDownVo up(AccountUpDto accountUpDto) throws Exception {
        try {
            log.info("account banlance up amount params:{}", accountUpDto);
            AccountVo accountVo = checkAccount(accountUpDto);
            if (!BusiEnum.FUNCODE_UP.getCode().equals(accountUpDto.getFunCode())){
                throw new BusinessException(ResultEnum.ERROR.getCode(),"上账功能码不正确");
            }
            log.info("account up infomation check completed:{}", accountVo);
            return accountTransactionImpl.up(accountUpDto);

        }catch (Exception e){
            log.error("account banlance up exception:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public AccountUpDownVo down(AccountUpDto accountUpDto) throws Exception {
        try{
            log.info("account banlance down amount params:{}", accountUpDto);
            AccountVo accountVo = checkAccount(accountUpDto);
            log.info("account down infomation check completed:{}", accountVo);
        }catch (Exception e){
            log.error("account banlance down exception:{}", e.getMessage());
            throw e;
        }
        return null;
    }

    public void checkParam(AccountVo accountVo) throws Exception {
        if (BusiEnum.FREEZE.getCode().equals(accountVo.getAccountStatus())
                || BusiEnum.CLOSE.getCode().equals(accountVo.getAccountStatus())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户状态不正常，无法进行资金操作");
        }
    }
    public AccountVo checkAccount(AccountUpDto accountUpDto) throws Exception {
        List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                .eq(AccountVo::getAccountNo, accountUpDto.getAccountNo())
        .eq(AccountVo::getAccountType, accountUpDto.getAccountType())
        .eq(AccountVo::getStoreId, accountUpDto.getStoreId()));
        if (CollectionUtils.isEmpty(accountVos)) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户号不存在");
        }
        AccountVo accountVo = accountVos.get(0);
        checkParam(accountVo);
        log.info("account infomation check completed:{}", accountVo);
        return accountVo;

    }
}
