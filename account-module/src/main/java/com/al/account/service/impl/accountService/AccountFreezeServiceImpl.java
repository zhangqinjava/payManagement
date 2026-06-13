package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountFreezeRiskDto;
import com.al.account.bean.dto.FreezeQueryDto;
import com.al.account.bean.vo.AccountFreezeResultVo;
import com.al.account.bean.vo.AccountFreezeVo;
import com.al.account.bean.vo.AccountfreezeDetailVo;
import com.al.account.mapper.AccountFreezeDetailMapper;
import com.al.account.mapper.AccountFreezeMapper;
import com.al.account.service.accountService.AccountFreezeService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AccountFreezeServiceImpl implements AccountFreezeService {
    @Autowired
    private AccountFreezeMapper freezeMapper;
    @Autowired
    private AccountFreezeDetailMapper freezeDetailMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private AccountFreezeTransaction accountFreezeTransaction;

    @Override
    public AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception {
        throw new BusinessException("请使用 /operation/freeze 进行资金冻结");
    }

    @Override
    public AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception {
        throw new BusinessException("请使用 /operation/unfreeze 进行资金解冻");
    }

    @Override
    public AccountFreezeResultVo riskFreeze(AccountFreezeRiskDto dto) throws Exception {
        RLock lock = redissonClient.getLock(Const.FREEZE_PREFIX + dto.getFreezeNo());
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (!locked) {
                throw new BusinessException("冻结单处理中，请勿重复提交");
            }
            AccountFreezeVo exist = getFreezeByNo(dto.getFreezeNo());
            if (Objects.nonNull(exist)) {
                throw new BusinessException("冻结单号重复");
            }
            return accountFreezeTransaction.riskFreeze(dto);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public AccountFreezeResultVo riskUnfreeze(AccountFreezeRiskDto dto) throws Exception {
        RLock lock = redissonClient.getLock(Const.FREEZE_PREFIX + dto.getFreezeNo());
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (!locked) {
                throw new BusinessException("解冻处理中，请勿重复提交");
            }
            return accountFreezeTransaction.riskUnfreeze(dto);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<AccountFreezeVo> queryFreeze(FreezeQueryDto dto) {
        return freezeMapper.selectList(Wrappers.lambdaQuery(AccountFreezeVo.class)
                .eq(dto.getMerchantNo() != null, AccountFreezeVo::getMerchantNo, dto.getMerchantNo())
                .eq(dto.getAccountNo() != null, AccountFreezeVo::getAcountNo, dto.getAccountNo())
                .eq(dto.getAccountType() != null, AccountFreezeVo::getAccountType, dto.getAccountType())
                .eq(dto.getFreezeNo() != null, AccountFreezeVo::getFreezeNo, dto.getFreezeNo())
                .eq(dto.getFreezeStatus() != null, AccountFreezeVo::getFreezeStatus, dto.getFreezeStatus())
                .ge(dto.getStartDate() != null, AccountFreezeVo::getCreateTime, dto.getStartDate())
                .le(dto.getEndDate() != null, AccountFreezeVo::getCreateTime, dto.getEndDate())
                .orderByDesc(AccountFreezeVo::getCreateTime));
    }

    @Override
    public List<AccountfreezeDetailVo> queryFreezeDetail(FreezeQueryDto dto) {
        return freezeDetailMapper.selectList(Wrappers.lambdaQuery(AccountfreezeDetailVo.class)
                .eq(dto.getFreezeNo() != null, AccountfreezeDetailVo::getFreezeNo, dto.getFreezeNo())
                .eq(dto.getMerchantNo() != null, AccountfreezeDetailVo::getMerchantNo, dto.getMerchantNo())
                .eq(dto.getAccountNo() != null, AccountfreezeDetailVo::getAccountNo, dto.getAccountNo())
                .eq(dto.getAccountType() != null, AccountfreezeDetailVo::getAccountType, dto.getAccountType())
                .orderByDesc(AccountfreezeDetailVo::getCreateTime));
    }

    private AccountFreezeVo getFreezeByNo(String freezeNo) {
        return freezeMapper.selectOne(Wrappers.lambdaQuery(AccountFreezeVo.class)
                .eq(AccountFreezeVo::getFreezeNo, freezeNo));
    }
}
