package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.*;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.account.mapper.AccountDtlMapper;
import com.al.account.service.accountService.AccountService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;
    @Autowired
    private AccountOpenMapper accountOpenMapper;
    @Autowired
    private AccountDtlMapper accountDtlMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Resource(name = "accountThreadPool")
    private Executor accountThreadPool;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public AccountOpenVo save(AccountDto accountDto) throws Exception{
        try {
            log.info("start open account infomation save:{}", accountDto);
            checkParam(accountDto);
            checkAccount(accountDto,false);
            log.info("end open account infomation save:{}", accountDto);
            String traceId = TraceUtil.createTraceId();
            if (StringUtils.isNullOrEmpty(accountDto.getAccountNo())) {
                accountDto.setAccountNo(traceId);
            }
            return accountTransactionImpl.save(accountDto);
        }catch (Exception e){
            log.error("open save account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String update(AccountDto accountDto) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountDto.getAccountNo());
        try {
            log.info("start open account information update:{}", accountDto);
            checkParam(accountDto);
            if (Objects.isNull(accountDto.getAccountNo())) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户号不能为空");
            }
            lock.lock();
            checkAccount(accountDto,true);
            log.info("end open account information update:{}", accountDto);
            // 等待并触发异常
            return accountTransactionImpl.update(accountDto);
        }catch (Exception e){
            log.error("open update account information error:{}", e.getMessage());
            throw e;
        }finally {
            try{
                if (lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }catch (IllegalMonitorStateException e){
                log.warn("lock already release held by current thread:{}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(AccountDto accountDto)throws Exception {
        try {
            log.info("start open account information delete:{}", accountDto);
            checkAccount(accountDto,true);
            log.info("end open account information delete:{}", accountDto);
            int delete = accountMapper.delete(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountDto.getAccountNo()));
            if (delete == 0) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "数据库删除失败");
            }
            return "删除成功";
        }catch (Exception e){
            log.error("open delete account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<AccountVo> query(AccountDto accountDto) {
        try {
            List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                    .eq(AccountVo::getAccountNo, accountDto.getAccountNo())
                    .eq(AccountVo::getMerchantNo, accountDto.getMerchantNo())
                     .eq(AccountVo::getAccountType, accountDto.getAccountType()));
            return accountVos;
        }catch (Exception e){
            log.error("open query account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public AccountBalanceVo queryBalance(AccountBalanceQueryDto dto) throws Exception {
        AccountVo account = accountMapper.selectOne(Wrappers.lambdaQuery(AccountVo.class)
                .eq(AccountVo::getAccountNo, dto.getAccountNo())
                .eq(AccountVo::getMerchantNo, dto.getMerchantNo())
                .eq(AccountVo::getAccountType, dto.getAccountType()));
        if (account == null) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户不存在");
        }
        BigDecimal frozen = account.getFrozenBalance() == null ? BigDecimal.ZERO : account.getFrozenBalance();
        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        return AccountBalanceVo.builder()
                .merchantNo(account.getMerchantNo())
                .accountNo(account.getAccountNo())
                .accountType(account.getAccountType())
                .balance(balance)
                .frozenBalance(frozen)
                .transitBalance(account.getTransitBalance())
                .availableBalance(balance.subtract(frozen))
                .accountStatus(account.getAccountStatus())
                .currency(account.getCurrency())
                .build();
    }

    @Override
    public List<AccountVo> listByMerchant(String merchantNo, String accountType) {
        return accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                .eq(AccountVo::getMerchantNo, merchantNo)
                .eq(accountType != null, AccountVo::getAccountType, accountType));
    }

    @Override
    public String freezeAccount(AccountStatusDto dto) throws Exception {
        return changeAccountStatus(dto, BusiEnum.FREEZE.getCode());
    }

    @Override
    public String closeAccount(AccountStatusDto dto) throws Exception {
        AccountVo account = accountMapper.selectOne(Wrappers.lambdaQuery(AccountVo.class)
                .eq(AccountVo::getAccountNo, dto.getAccountNo())
                .eq(AccountVo::getMerchantNo, dto.getMerchantNo())
                .eq(AccountVo::getAccountType, dto.getAccountType()));
        if (account == null) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户不存在");
        }
        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        BigDecimal frozen = account.getFrozenBalance() == null ? BigDecimal.ZERO : account.getFrozenBalance();
        BigDecimal transit = account.getTransitBalance() == null ? BigDecimal.ZERO : account.getTransitBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0 || frozen.compareTo(BigDecimal.ZERO) != 0
                || transit.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户余额不为零，无法销户");
        }
        return changeAccountStatus(dto, BusiEnum.CLOSE.getCode());
    }

    @Override
    public List<AccountOpenFlowVo> queryOpenFlow(AccountOpenFlowQueryDto dto) {
        return accountOpenMapper.selectList(Wrappers.lambdaQuery(AccountOpenFlowVo.class)
                .eq(dto.getMerchantNo() != null, AccountOpenFlowVo::getMerchantNo, dto.getMerchantNo())
                .eq(dto.getAccountNo() != null, AccountOpenFlowVo::getAccountNo, dto.getAccountNo())
                .ge(dto.getStartDate() != null, AccountOpenFlowVo::getCreateTime, dto.getStartDate())
                .le(dto.getEndDate() != null, AccountOpenFlowVo::getCreateTime, dto.getEndDate())
                .orderByDesc(AccountOpenFlowVo::getCreateTime));
    }

    @Override
    public PageVo<AccountVo> adminList(AdminAccountListDto dto) {
        Page<AccountVo> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<AccountVo> result = accountMapper.selectPage(page, Wrappers.lambdaQuery(AccountVo.class)
                .eq(dto.getMerchantNo() != null, AccountVo::getMerchantNo, dto.getMerchantNo())
                .eq(dto.getAccountNo() != null, AccountVo::getAccountNo, dto.getAccountNo())
                .eq(dto.getAccountType() != null, AccountVo::getAccountType, dto.getAccountType())
                .eq(dto.getAccountStatus() != null, AccountVo::getAccountStatus, dto.getAccountStatus())
                .orderByDesc(AccountVo::getUpdateTime));
        return PageVo.<AccountVo>builder()
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .records(result.getRecords())
                .build();
    }

    @Override
    public List<ReconcileDailyVo> reconcileDaily(ReconcileDailyDto dto) {
        List<AccountVo> accounts = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                .eq(dto.getMerchantNo() != null, AccountVo::getMerchantNo, dto.getMerchantNo())
                .eq(dto.getAccountNo() != null, AccountVo::getAccountNo, dto.getAccountNo()));
        List<ReconcileDailyVo> result = new ArrayList<>();
        for (AccountVo account : accounts) {
            QuerySummaryDto summaryDto = new QuerySummaryDto();
            summaryDto.setMerchantNo(account.getMerchantNo());
            summaryDto.setAccountNo(account.getAccountNo());
            summaryDto.setAccountType(account.getAccountType());
            summaryDto.setStartDate(dto.getReconcileDate());
            summaryDto.setEndDate(dto.getReconcileDate());
            AccountSummaryVo summary = accountDtlMapper.querySummary(summaryDto);
            BigDecimal credit = summary != null && summary.getTotalCredit() != null ? summary.getTotalCredit() : BigDecimal.ZERO;
            BigDecimal debit = summary != null && summary.getTotalDebit() != null ? summary.getTotalDebit() : BigDecimal.ZERO;
            BigDecimal freeze = summary != null && summary.getTotalFreeze() != null ? summary.getTotalFreeze() : BigDecimal.ZERO;
            BigDecimal unfreeze = summary != null && summary.getTotalUnfreeze() != null ? summary.getTotalUnfreeze() : BigDecimal.ZERO;
            BigDecimal closing = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
            BigDecimal netChange = credit.subtract(debit);
            BigDecimal opening = closing.subtract(netChange);
            boolean balanced = opening.add(netChange).compareTo(closing) == 0;
            result.add(ReconcileDailyVo.builder()
                    .reconcileDate(dto.getReconcileDate())
                    .merchantNo(account.getMerchantNo())
                    .accountNo(account.getAccountNo())
                    .accountType(account.getAccountType())
                    .openingBalance(opening)
                    .closingBalance(closing)
                    .creditAmount(credit)
                    .debitAmount(debit)
                    .freezeAmount(freeze)
                    .unfreezeAmount(unfreeze)
                    .balanced(balanced)
                    .build());
        }
        return result;
    }

    private String changeAccountStatus(AccountStatusDto dto, String targetStatus) throws Exception {
        AccountDto accountDto = new AccountDto();
        accountDto.setFlow(dto.getFlow());
        accountDto.setAccountNo(dto.getAccountNo());
        accountDto.setMerchantNo(dto.getMerchantNo());
        accountDto.setAccountType(dto.getAccountType());
        accountDto.setAccountStatus(targetStatus);
        accountDto.setModifyUser(dto.getModifyUser());
        accountDto.setRemark(dto.getRemark());
        accountDto.setChannelCode(BusiEnum.WX.getCode());
        accountDto.setChannelAccountNo(dto.getAccountNo());
        return update(accountDto);
    }

    public void checkParam(AccountDto accountDto) throws Exception{
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountType()) &&!BusiEnum.contains(accountDto.getAccountType())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户类型不正确");
        }
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountStatus()) && !BusiEnum.contains(accountDto.getAccountStatus())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户状态不正确");
        }
    }

    /**
     * 校验账户是否存在
     * 校验流水是否存在
     * flag=true 校验账户是否已经存在
     * flag=false 校验账户不能存在
     * @param accountDto
     * @param flag
     * @throws Exception
     */
    public void checkAccount(AccountDto accountDto,boolean flag) throws Exception{
        try {
            log.info("start open account information check:{}", accountDto);
            CompletableFuture.supplyAsync(() ->
                    accountMapper.selectCount(
                            Wrappers.lambdaQuery(AccountVo.class)
                                    .eq(AccountVo::getAccountNo, accountDto.getAccountNo())
                                    .eq(AccountVo::getMerchantNo, accountDto.getMerchantNo())
                                    .eq(AccountVo::getAccountType, accountDto.getAccountType())
                    ), accountThreadPool
            ).thenCombine(
                    CompletableFuture.supplyAsync(() ->
                            accountOpenMapper.selectCount(
                                    Wrappers.lambdaQuery(AccountOpenFlowVo.class)
                                            .eq(AccountOpenFlowVo::getFlow, accountDto.getFlow())
                            ), accountThreadPool
                    ),
                    (count, flowCount) -> {
                        if (flag) {
                            if (count == 0) {
                                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户信息不存在");
                            }
                        }else{
                            if (count > 0) {
                                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户已经存在");
                            }
                        }
                        if (flowCount > 0) {
                            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户操作流水号重复");
                        }
                        return null;
                    }
            ).join();
            log.info("end open account information check:{}", accountDto);
        }catch (Exception e){
            if (e.getCause() instanceof BusinessException) {
                throw (BusinessException) e.getCause();
            }
            throw e;
        }
    }

}
