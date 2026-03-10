package com.al.account.service.impl.accountService;

import com.al.account.bean.vo.AccountFreezeResultVo;
import com.al.common.business.BusiEnum;
import org.springframework.stereotype.Service;
import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.vo.AccountFreezeVo;
import com.al.account.mapper.AccountDtlMapper;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountFreezeMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.service.accountService.AccountFreezeService;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Service
@Slf4j
public class AccountFreezeServiceImpl implements AccountFreezeService {
    @Autowired
    private  AccountMapper accountMapper;
    @Autowired
    private  AccountFreezeMapper freezeMapper;
    @Autowired
    private  AccountFlowMapper flowMapper;
    @Autowired
    private AccountDtlMapper dtlMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception {
        RLock lock=null;
        boolean flag=false;
        try {
            log.info("account freeze risk start request param:{} ",accountFreezeDto);
            if (!BusiEnum.FUNCODE_FREEZE.getCode().equals(accountFreezeDto.getFunCode())){
                throw new BusinessException("功能码不正确");
            }
            lock= redissonClient.getLock(Const.FREEZE_PREFIX + accountFreezeDto.getFlowNo());
            if (flag=lock.tryLock()){
                AccountFreezeVo accountFreezeVo = getFreezeList(accountFreezeDto);
                if (Objects.nonNull(accountFreezeVo)){
                    log.info("request account freeze flow repeat:{]", accountFreezeVo);
                    throw new BusinessException("账户资金冻结流水号重复" );
                }

            }else{
                throw new BusinessException("账户资金冻结流水号重复");
            }
            log.info("freeze risk trigger start request param:{}", accountFreezeDto);
            return null;
        }catch (Exception e){
            log.error("freeze risk trigger error:{}",e.getMessage());
            throw e;
        }finally {
            if(flag && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    @Override
    public AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception {
        try {
            return null;
        }catch (Exception e){
            log.error("unfreeze risk trigger error:{}",e.getMessage());
            throw e;
        }
    }
    private AccountFreezeVo getFreezeList(AccountFreezeDto accountFreezeDto){
        return freezeMapper.selectOne(Wrappers.lambdaQuery(AccountFreezeVo.class)
                .eq(AccountFreezeVo::getFreezeNo, accountFreezeDto.getFlowNo()));
    }
}

