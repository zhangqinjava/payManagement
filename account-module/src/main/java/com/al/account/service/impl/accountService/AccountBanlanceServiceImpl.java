package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.service.accountService.AccountBanlanceService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 账户余额操作
 */
@Service
@Slf4j
public class AccountBanlanceServiceImpl implements AccountBanlanceService {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountFlowMapper accountFlowMapper;
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;
    @Resource(name = "accountThreadPool")
    private Executor accountThreadPool;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception {
        RLock lock= redissonClient.getLock(Const.UP_LOCK_PREFIX + accountUpDownDto.getAccountNo());
        try {
            log.info("account banlance up amount params:{}", accountUpDownDto);
            if (!BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode())){
                throw new BusinessException(ResultEnum.ERROR.getCode(),"上账功能码不正确");
            }
            lock.lock();
            checkAccount(accountUpDownDto);
            log.info("account up infomation check completed:{}",accountUpDownDto);
            return accountTransactionImpl.upDown(accountUpDownDto,true);
        }catch (Exception e){
            log.error("account banlance up exception:{}", e.getMessage());
            throw e;
        }finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("unlock up lock failed, maybe already released", e);
            }
        }
    }

    @Override
    public AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountUpDownDto.getAccountNo());
        try{
            log.info("account banlance down amount params:{}", accountUpDownDto);
            if (!BusiEnum.FUNCODE_DOWN.getCode().equals(accountUpDownDto.getFunCode())){
                throw new BusinessException(ResultEnum.ERROR.getCode(),"下账功能码不正确");
            }
            lock.lock();
            checkAccount(accountUpDownDto);
            log.info("account up infomation check completed");
            return accountTransactionImpl.upDown(accountUpDownDto,false);
        }catch (Exception e){
            log.error("account banlance down exception:{}", e.getMessage());
            throw e;
        }finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("unlock up lock failed, maybe already released", e);
            }
        }
    }

    @Override
    public AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception {
        List<String> accountNos = Stream.of(accountTransferDto.getOutAccountNo(), accountTransferDto.getInAccountNo())
                .sorted().collect(Collectors.toList());
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountNos.get(0));
        RLock lock1 = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountNos.get(1));
        RLock multiLock = redissonClient.getMultiLock(lock, lock1);
        try {
            log.info("account banlance transfer amount params:{}", accountTransferDto);
            if (!BusiEnum.FUNCODE_TRANSFER.getCode().equals(accountTransferDto.getFunCode())){
                throw new BusinessException(ResultEnum.ERROR.getCode(), "转账交易码错误" );
            }
            multiLock.lock();
            checkAccount(accountTransferDto);
            return accountTransactionImpl.transfer(accountTransferDto);
        }catch (Exception e){
            log.error("account banlance transfer exception:{}", e.getMessage());
            throw e;
        }finally {
            try {
                if (multiLock.isHeldByCurrentThread()) {
                    multiLock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("unlock multiLock failed, maybe already released", e);
            }
        }
    }

    @Override
    public AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountFreezeDto.getAccountNo());
        try{
            log.info("account banlance freeze params:{}", accountFreezeDto);
            if (!BusiEnum.FUNCODE_FREEZE.getCode().equals(accountFreezeDto.getFunCode())){
                throw new BusinessException("功能码不正确");
            }
            lock.lock();
            checkAccount(accountFreezeDto);
            return accountTransactionImpl.freezeResultVo(accountFreezeDto, true);
        }catch (Exception e){
            log.info("account banlance freeze exception:{}", e.getMessage());
            throw e;
        }finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }catch (IllegalMonitorStateException e) {
                log.warn("freeze unlock up lock failed, maybe already released", e);
            }
        }
    }

    @Override
    public AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + accountFreezeDto.getAccountNo());
        try{
            log.info("account banlance unfreeze params:{}", accountFreezeDto);
            if (!BusiEnum.FUNCODE_UNFREEZE.getCode().equals(accountFreezeDto.getFunCode())){
                throw new BusinessException("功能码不正确");
            }
            lock.lock();
            checkAccount(accountFreezeDto);
            return accountTransactionImpl.freezeResultVo(accountFreezeDto, false);
        }catch (Exception e){
            log.info("account banlance unfreeze exception:{}", e.getMessage());
            throw e;
        }finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }catch (IllegalMonitorStateException e) {
                log.warn("unfreeze unlock up lock failed, maybe already released", e);
            }
        }
    }

    public void  checkAccount(AccountFreezeDto accountFreezeDto){
        log.info("account freeze banlance check params:{}", accountFreezeDto);
        try{
            CompletableFuture.supplyAsync(()->{
                return accountMapper.selectOne(Wrappers.lambdaQuery(AccountVo.class)
                        .eq(AccountVo::getAccountNo, accountFreezeDto.getAccountNo())
                        .eq(AccountVo::getAccountType, accountFreezeDto.getAccountType())
                        .eq(AccountVo::getStoreId, accountFreezeDto.getStoreId()));
            },accountThreadPool).thenCombine(CompletableFuture.supplyAsync(()->{
                return accountFlowMapper.selectOne(Wrappers.lambdaQuery(AccountFlowVo.class)
                        .eq(AccountFlowVo::getFlowNo, accountFreezeDto.getFlowNo()));
            },accountThreadPool), (account,flow)->{
                if (Objects.isNull(account)){
                    throw new BusinessException("账户信息不存在");
                }else if (!BusiEnum.NORMAL.getCode().equals(account.getAccountStatus())){
                    throw new BusinessException("账户状态不正确");
                }
                if (Objects.nonNull(flow)){
                    throw new BusinessException("流水号重复" );
                }
                return null;
            }).join();
        }catch (Exception e){
            log.error("account banlance freeze exception:{}", e.getCause());
            if (e.getCause() instanceof BusinessException){
                throw (BusinessException) e.getCause();
            }
            throw e;
        }
    }

    public void checkAccount(AccountUpDownDto accountUpDownDto) throws Exception {
        log.info("account up or down banlance check amount status and flow information:{}", accountUpDownDto);
        try {
            CompletableFuture.supplyAsync(() ->
                    accountMapper.selectOne(
                            Wrappers.lambdaQuery(AccountVo.class)
                                    .eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo())
                                    .eq(AccountVo::getAccountType, accountUpDownDto.getAccountType())
                                    .eq(AccountVo::getStoreId, accountUpDownDto.getStoreId())
                    ), accountThreadPool
            ).thenCombine(
                    CompletableFuture.supplyAsync(() ->
                            accountFlowMapper.selectOne(
                                    Wrappers.lambdaQuery(AccountFlowVo.class)
                                            .eq(AccountFlowVo::getFlowNo, accountUpDownDto.getFlowNo())
                            ), accountThreadPool
                    ),
                    (account, flow) -> {
                        log.info("account information :{}", account);
                        if (account == null) {
                            throw new BusinessException(ResultEnum.ERROR.getCode(), "账户号不存在");
                        } else {
                            if (BusiEnum.FREEZE.getCode().equals(account.getAccountStatus())
                                    || BusiEnum.CLOSE.getCode().equals(account.getAccountStatus())) {
                                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户状态不正常，无法进行资金操作");
                            }
                        }
                        if (flow != null) {
                            throw new BusinessException(ResultEnum.ERROR.getCode(), "流水号重复");
                        }
                        return null; // Void
                    }
            ).join();
        }catch (Exception e) {
            log.error("account banlance up or down exception:{}", e.getCause());
            if (e.getCause() instanceof BusinessException) {
                throw (BusinessException) e.getCause();
            }
            throw e;
        }
    }
    public void checkAccount(AccountTransferDto accountTransferDto) throws Exception {
        try {
            log.info("account transfer banlance check amount params:{}", accountTransferDto);
            CompletableFuture.supplyAsync(() -> {
                return accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                        .in(AccountVo::getAccountNo, Arrays.asList(accountTransferDto.getOutAccountNo(), accountTransferDto.getInAccountNo())));
            }).thenCombine(CompletableFuture.supplyAsync(() -> {
                return accountFlowMapper.selectOne(Wrappers.lambdaQuery(AccountFlowVo.class)
                        .eq(AccountFlowVo::getFlowNo, accountTransferDto.getFlowNo()));
            }), (result1, result2) -> {
                if (CollectionUtils.isEmpty(result1) || result1.size() != 2) {
                    throw new BusinessException("转账双方账户信息不正确");
                } else {
                    List<AccountVo> collect = result1.stream().filter(accountVo ->
                            BusiEnum.NORMAL.getCode().equals(accountVo.getAccountStatus())
                    ).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(collect) || collect.size() != 2) {
                        throw new BusinessException("转账双方账户状态不正确");
                    }
                }
                if (result2 != null) {
                    throw new BusinessException("转账流水号请求重复");
                }
                return null;
            }).join();
        }catch (Exception e){
            log.error("account banlance transfer exception:{}", e.getCause());
            if (e.getCause() instanceof BusinessException) {
                throw (BusinessException) e.getCause();
            }
            throw e;
        }
    }
}
