package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.*;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountDtlMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
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
    private AccountDtlMapper accountDtlMapper;
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;
    @Resource(name = "accountThreadPool")
    private Executor accountThreadPool;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception {
        return executeUpDown(accountUpDownDto, BusiEnum.FUNCODE_UP.getCode(), "上账功能码不正确", "up");
    }

    @Override
    public AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception {
        return executeUpDown(accountUpDownDto, BusiEnum.FUNCODE_DOWN.getCode(), "下账功能码不正确", "down");
    }

    @Override
    public AccountUpDownVo downWay(AccountUpDownDto accountUpDownDto) throws Exception {
        return executeUpDown(accountUpDownDto, BusiEnum.FUNCODE_DOWNWAY.getCode(), "下账到在途功能码不正确", "downway");
    }

    @Override
    public AccountUpDownVo transitDown(AccountUpDownDto accountUpDownDto) throws Exception {
        return executeUpDown(accountUpDownDto, BusiEnum.FUNCODE_TRANSIT_DOWN.getCode(), "在途下账功能码不正确", "transitDown");
    }

    @Override
    public AccountUpDownVo transitUp(AccountUpDownDto accountUpDownDto) throws Exception {
        return executeUpDown(accountUpDownDto, BusiEnum.FUNCODE_TRANSIT_UP.getCode(), "在途上账到总账户功能码不正确", "transitUp");
    }

    @Override
    public List<AccountQueryDtlVo> query(AccountQueryDto accountQueryDto) throws Exception {
        try {
            log.info("account detail query start params:{}", accountQueryDto);
            return accountDtlMapper.queryDetailList(accountQueryDto);
        } catch (Exception e) {
            log.error("query account transfer detail information exception:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public AccountFlowVo queryFlow(AccountFlowQueryDto dto) throws Exception {
        AccountFlowVo flow = accountFlowMapper.selectOne(
                Wrappers.lambdaQuery(AccountFlowVo.class).eq(AccountFlowVo::getFlowNo, dto.getFlowNo())
        );
        if (flow == null) {
            throw new BusinessException(ResultEnum.ERROR.getCode(), "流水不存在");
        }
        return flow;
    }

    @Override
    public AccountSummaryVo querySummary(QuerySummaryDto dto) throws Exception {
        AccountSummaryVo summary = accountDtlMapper.querySummary(dto);
        if (summary == null) {
            summary = AccountSummaryVo.builder()
                    .merchantNo(dto.getMerchantNo())
                    .accountNo(dto.getAccountNo())
                    .accountType(dto.getAccountType())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .build();
        }
        return summary;
    }

    @Override
    public AccountTransferVo settleClear(SettleClearDto dto) throws Exception {
        return transfer(toTransferDto(dto));
    }

    @Override
    public AccountUpDownVo settlePayout(SettlePayoutDto dto) throws Exception {
        AccountUpDownDto upDownDto = new AccountUpDownDto();
        upDownDto.setFlowNo(dto.getFlowNo());
        upDownDto.setAccountNo(dto.getAccountNo());
        upDownDto.setMerchantNo(dto.getMerchantNo());
        upDownDto.setAccountType(dto.getAccountType());
        upDownDto.setChannelCode(dto.getChannelCode());
        upDownDto.setBizType(dto.getBizType());
        upDownDto.setBizOrderNo(dto.getBizOrderNo());
        upDownDto.setBizOrderDate(dto.getBizOrderDate());
        upDownDto.setBizOrderTime(dto.getBizOrderTime());
        upDownDto.setAmount(dto.getAmount());
        upDownDto.setRemark(dto.getRemark());
        upDownDto.setFunCode(BusiEnum.FUNCODE_TRANSIT_DOWN.getCode());
        return transitDown(upDownDto);
    }

    @Override
    public BatchUpResultVo batchUp(BatchUpDto dto) throws Exception {
        BatchUpResultVo result = BatchUpResultVo.builder()
                .successCount(0)
                .failCount(0)
                .successList(new ArrayList<>())
                .failMessages(new ArrayList<>())
                .build();
        for (AccountUpDownDto item : dto.getItems()) {
            try {
                if (!BusiEnum.FUNCODE_UP.getCode().equals(item.getFunCode())) {
                    item.setFunCode(BusiEnum.FUNCODE_UP.getCode());
                }
                AccountUpDownVo upResult = up(item);
                result.getSuccessList().add(upResult);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.getFailMessages().add(item.getFlowNo() + ":" + e.getMessage());
                result.setFailCount(result.getFailCount() + 1);
            }
        }
        return result;
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
            if (!BusiEnum.FUNCODE_TRANSFER.getCode().equals(accountTransferDto.getFunCode())) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "转账交易码错误");
            }
            multiLock.lock();
            checkAccount(accountTransferDto);
            return accountTransactionImpl.transfer(accountTransferDto);
        } catch (Exception e) {
            log.error("account banlance transfer exception:{}", e.getMessage());
            throw e;
        } finally {
            unlockSafely(multiLock, "transfer");
        }
    }

    @Override
    public AccountFreezeResultVo freeze(AccountFreezeDto accountFreezeDto) throws Exception {
        return executeFreeze(accountFreezeDto, BusiEnum.FUNCODE_FREEZE.getCode(), true);
    }

    @Override
    public AccountFreezeResultVo unfreeze(AccountFreezeDto accountFreezeDto) throws Exception {
        return executeFreeze(accountFreezeDto, BusiEnum.FUNCODE_UNFREEZE.getCode(), false);
    }

    private AccountUpDownVo executeUpDown(AccountUpDownDto dto, String expectedFunCode,
                                          String funCodeErrorMsg, String operation) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + dto.getAccountNo());
        try {
            log.info("account banlance {} amount params:{}", operation, dto);
            if (!expectedFunCode.equals(dto.getFunCode())) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), funCodeErrorMsg);
            }
            lock.lock();
            checkAccount(dto);
            log.info("account {} information check completed:{}", operation, dto);
            return accountTransactionImpl.upDown(dto);
        } catch (Exception e) {
            log.error("account banlance {} exception:{}", operation, e.getMessage());
            throw e;
        } finally {
            unlockSafely(lock, operation);
        }
    }

    private AccountFreezeResultVo executeFreeze(AccountFreezeDto dto, String expectedFunCode, boolean freeze) throws Exception {
        RLock lock = redissonClient.getLock(Const.UP_LOCK_PREFIX + dto.getAccountNo());
        String operation = freeze ? "freeze" : "unfreeze";
        try {
            log.info("account banlance {} params:{}", operation, dto);
            if (!expectedFunCode.equals(dto.getFunCode())) {
                throw new BusinessException("功能码不正确");
            }
            lock.lock();
            checkAccount(dto);
            return accountTransactionImpl.freezeResultVo(dto, freeze);
        } catch (Exception e) {
            log.info("account banlance {} exception:{}", operation, e.getMessage());
            throw e;
        } finally {
            unlockSafely(lock, operation);
        }
    }

    private AccountTransferDto toTransferDto(SettleClearDto dto) {
        AccountTransferDto transferDto = new AccountTransferDto();
        transferDto.setFlowNo(dto.getFlowNo());
        transferDto.setOutAccountNo(dto.getOutAccountNo());
        transferDto.setOutMerchantNo(dto.getOutMerchantNo());
        transferDto.setOutAccountType(dto.getOutAccountType());
        transferDto.setInAccountNo(dto.getInAccountNo());
        transferDto.setInMerchantNo(dto.getInMerchantNo());
        transferDto.setInAccountType(dto.getInAccountType());
        transferDto.setAmount(dto.getAmount());
        transferDto.setBizType(dto.getBizType());
        transferDto.setBizOrderNo(dto.getBizOrderNo());
        transferDto.setBizOrderDate(dto.getBizOrderDate());
        transferDto.setBizOrderTime(dto.getBizOrderTime());
        transferDto.setChannelCode(dto.getChannelCode() != null ? dto.getChannelCode() : BusiEnum.WX.getCode());
        transferDto.setRemark(dto.getRemark());
        transferDto.setFunCode(BusiEnum.FUNCODE_TRANSFER.getCode());
        return transferDto;
    }

    private void unlockSafely(RLock lock, String operation) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (IllegalMonitorStateException e) {
            log.warn("unlock {} lock failed, maybe already released", operation, e);
        }
    }

    private void rethrowCheckException(Exception e, String operation) throws Exception {
        log.error("account banlance {} check exception:{}", operation, e.getCause());
        if (e.getCause() instanceof BusinessException) {
            throw (BusinessException) e.getCause();
        }
        throw e;
    }

    private <T> T runCheck(Supplier<T> checkTask, String operation) throws Exception {
        try {
            return checkTask.get();
        } catch (Exception e) {
            rethrowCheckException(e, operation);
            return null;
        }
    }

    public void checkAccount(AccountFreezeDto accountFreezeDto) throws Exception {
        log.info("account freeze banlance check params:{}", accountFreezeDto);
        runCheck(() -> CompletableFuture.supplyAsync(() ->
                accountMapper.selectOne(Wrappers.lambdaQuery(AccountVo.class)
                        .eq(AccountVo::getAccountNo, accountFreezeDto.getAccountNo())
                        .eq(AccountVo::getAccountType, accountFreezeDto.getAccountType())
                        .eq(AccountVo::getMerchantNo, accountFreezeDto.getMerchantNo()))
        , accountThreadPool).thenCombine(CompletableFuture.supplyAsync(() ->
                accountFlowMapper.selectOne(Wrappers.lambdaQuery(AccountFlowVo.class)
                        .eq(AccountFlowVo::getFlowNo, accountFreezeDto.getFlowNo()))
        , accountThreadPool), (account, flow) -> {
            if (Objects.isNull(account)) {
                throw new BusinessException("账户信息不存在");
            } else if (!BusiEnum.NORMAL.getCode().equals(account.getAccountStatus())) {
                throw new BusinessException("账户状态不正确");
            }
            if (Objects.nonNull(flow)) {
                throw new BusinessException("流水号重复");
            }
            return null;
        }).join(), "freeze");
    }

    public void checkAccount(AccountUpDownDto accountUpDownDto) throws Exception {
        log.info("account up or down banlance check amount status and flow information:{}", accountUpDownDto);
        runCheck(() -> CompletableFuture.supplyAsync(() ->
                accountMapper.selectOne(
                        Wrappers.lambdaQuery(AccountVo.class)
                                .eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo())
                                .eq(AccountVo::getAccountType, accountUpDownDto.getAccountType())
                                .eq(AccountVo::getMerchantNo, accountUpDownDto.getMerchantNo())
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
                    } else if (BusiEnum.FREEZE.getCode().equals(account.getAccountStatus())
                            || BusiEnum.CLOSE.getCode().equals(account.getAccountStatus())) {
                        throw new BusinessException(ResultEnum.ERROR.getCode(), "账户状态不正常，无法进行资金操作");
                    }
                    if (flow != null) {
                        throw new BusinessException(ResultEnum.ERROR.getCode(), "流水号重复");
                    }
                    return null;
                }
        ).join(), "upDown");
    }

    public void checkAccount(AccountTransferDto accountTransferDto) throws Exception {
        log.info("account transfer banlance check amount params:{}", accountTransferDto);
        runCheck(() -> CompletableFuture.supplyAsync(() ->
                accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class)
                        .in(AccountVo::getAccountNo, Arrays.asList(accountTransferDto.getOutAccountNo(), accountTransferDto.getInAccountNo())))
        , accountThreadPool).thenCombine(CompletableFuture.supplyAsync(() ->
                accountFlowMapper.selectOne(Wrappers.lambdaQuery(AccountFlowVo.class)
                        .eq(AccountFlowVo::getFlowNo, accountTransferDto.getFlowNo()))
        , accountThreadPool), (result1, result2) -> {
            if (CollectionUtils.isEmpty(result1) || result1.size() != 2) {
                throw new BusinessException("转账双方账户信息不正确");
            }
            List<AccountVo> collect = result1.stream().filter(accountVo ->
                    BusiEnum.NORMAL.getCode().equals(accountVo.getAccountStatus())
            ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect) || collect.size() != 2) {
                throw new BusinessException("转账双方账户状态不正确");
            }
            if (result2 != null) {
                throw new BusinessException("转账流水号请求重复");
            }
            return null;
        }).join(), "transfer");
    }
}
