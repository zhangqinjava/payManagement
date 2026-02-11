package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountDtlMapper;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class AccountTransactionImpl {
    @Autowired
    private AccountOpenMapper accountOpenMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountFlowMapper accountFlowMapper;
    @Autowired
    private AccountDtlMapper accountDtlMapper;
    @Autowired
    private RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountOpenVo save(AccountDto accountDto) throws Exception {
        try {
            log.info("start open account infomation save:{}", accountDto);
            AccountVo build = AccountVo.builder()
                    .accountNo(accountDto.getAccountNo())
                    .storeId(accountDto.getStoreId())
                    .channelCode(accountDto.getChannelCode())
                    .channelAccountNo(accountDto.getChannelAccountNo())
                    .accountStatus(BusiEnum.NORMAL.getCode())
                    .accountType(accountDto.getAccountType())
                    .balance(BigDecimal.ZERO)
                    .currency(BusiEnum.RMB.getCode())
                    .frozenBalance(BigDecimal.ZERO)
                    .transitBalance(BigDecimal.ZERO)
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .remark(accountDto.getRemark())
                    .build();
            AccountOpenFlowVo accountFlow = AccountOpenFlowVo.builder().accountNo(accountDto.getAccountNo())
                    .accountType(accountDto.getAccountType())
                    .accountNo(accountDto.getAccountNo())
                    .storeId(accountDto.getStoreId())
                    .currency(accountDto.getCurrency())
                    .channelAccountNo(accountDto.getChannelAccountNo())
                    .flow(accountDto.getFlow())
                    .currency(BusiEnum.RMB.getCode())
                    .channelCode(accountDto.getChannelCode())
                    .channelAccountNo(accountDto.getChannelAccountNo())
                    .operator(accountDto.getOperation() == null ? "system" : accountDto.getOperation())
                    .openStatus(BusiEnum.NORMAL.getCode())
                    .modifyUser(accountDto.getModifyUser() == null ? "system" : accountDto.getModifyUser())
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date())).build();
            accountMapper.insert(build);
            accountOpenMapper.insert(accountFlow);
            log.info("end open account infomation save success:{}", build);
            return AccountOpenVo.builder()
                    .accountNo(accountDto.getAccountNo())
                    .storeId(accountDto.getStoreId())
                    .channelCode(accountDto.getChannelCode())
                    .channelAccountNo(accountDto.getChannelAccountNo())
                    .accountStatus(BusiEnum.NORMAL.getCode())
                    .accountType(accountDto.getAccountType())
                    .build();
        } catch (Exception e) {
            log.error("open save account information error:{}", e.getMessage());
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "重复开户");
            } else if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "开户失败");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public String update(AccountDto accountDto) throws Exception {
        AccountVo build = AccountVo.builder()
                .accountStatus(accountDto.getAccountStatus())
                .storeId(accountDto.getStoreId())
                .accountNo(accountDto.getAccountNo())
                .updateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT))).build();

        int update = accountMapper.update(build, Wrappers.lambdaUpdate(AccountVo.class)
                .eq(AccountVo::getAccountNo, accountDto.getAccountNo())
                .eq(AccountVo::getStoreId, accountDto.getStoreId())
                .eq(AccountVo::getChannelCode, accountDto.getChannelCode()));
        if (update == 0) {
            return "更新失败";
        }
        AccountOpenFlowVo accountFlow = AccountOpenFlowVo.builder().accountNo(accountDto.getAccountNo())
                .accountType(accountDto.getAccountType())
                .accountNo(accountDto.getAccountNo())
                .storeId(accountDto.getStoreId())
                .currency(accountDto.getCurrency())
                .channelAccountNo(accountDto.getChannelAccountNo())
                .flow(accountDto.getFlow())
                .currency(BusiEnum.RMB.getCode())
                .channelCode(accountDto.getChannelCode())
                .channelAccountNo(accountDto.getChannelAccountNo())
                .operator(accountDto.getOperation() == null ? "system" : accountDto.getOperation())
                .openStatus(accountDto.getAccountStatus())
                .modifyUser(accountDto.getModifyUser() == null ? "system" : accountDto.getModifyUser())
                .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                .updateTime(DateFormat.getDateTimeInstance().format(new Date())).build();
        accountOpenMapper.insert(accountFlow);
        return "更新成功";
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountUpDownVo up(AccountUpDownDto accountUpDownDto) throws Exception {
        try {
            int rows = accountMapper.update(
                    null,
                    Wrappers.lambdaUpdate(AccountVo.class)
                            .eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo())
                            .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                            // 方式一：直接拼接字符串 (数值类型是安全的)
                            .setSql("balance = balance + " + new BigDecimal(accountUpDownDto.getAmount()))
                            .setSql("update_time = now()")
            );
            if (rows == 0) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户上账失败，请检查账户信息");
            }
            List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo()));
            AccountVo result = accountVos.get(0);
            log.info("账户更新后的结果:{}", result);
            AccountFlowVo build = AccountFlowVo.builder()
                    .flowNo(accountUpDownDto.getFlowNo())
                    .inAccountNo(accountUpDownDto.getAccountNo())
                    .bizType(accountUpDownDto.getBizType())
                    .funCode(accountUpDownDto.getFunCode())
                    .inStoreId(accountUpDownDto.getStoreId())
                    .in_account_type(accountUpDownDto.getAccountType())
                    .amount(new BigDecimal(accountUpDownDto.getAmount()))
                    .bizOrderNo(accountUpDownDto.getBizOrderNo())
                    .bizOrderDate(accountUpDownDto.getBizOrderDate())
                    .bizOrderTime(accountUpDownDto.getBizOrderTime())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .remark(accountUpDownDto.getRemark())
                    .build();
            accountFlowMapper.insert(build);
            AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                    .storeId(accountUpDownDto.getStoreId())
                    .accountType(accountUpDownDto.getAccountType())
                    .flowDtlNo(TraceUtil.createTraceId())
                    .flowNo(accountUpDownDto.getFlowNo())
                    .amount(new BigDecimal(accountUpDownDto.getAmount()))
                    .curBalance(result.getBalance())
                    .bizType(accountUpDownDto.getBizType())
                    .fundDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                    .funCode(accountUpDownDto.getFunCode())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
            accountDtlMapper.insert(accountDtlVo);
            AccountUpDownVo accountUpDownVo = AccountUpDownVo.builder().accountNo(accountUpDownDto.getAccountNo())
                    .accountType(result.getAccountType())
                    .flowNo(accountUpDownDto.getFlowNo())
                    .funCode(accountUpDownDto.getFunCode())
                    .amount(new BigDecimal(accountUpDownDto.getAmount()))
                    .funDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                    .bizType(accountUpDownDto.getBizType())
                    .channel_code(accountUpDownDto.getChannelCode())
                    .curBalance(result.getBalance())
                    .build();
            log.info("account up completed:{}", accountUpDownVo);
            return accountUpDownVo;
        } catch (Exception e) {
            log.error("transaction operation up banlance exception:{}", e.getMessage());
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "流水号重复");
            } else {
                throw e;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountUpDownVo down(AccountUpDownDto accountUpDownDto) throws Exception {
        try {
            int update = accountMapper.update(null, Wrappers.lambdaUpdate(AccountVo.class)
                        .eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo())
                        .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                        .ge(AccountVo::getBalance, accountUpDownDto.getAmount())//金额必须大于等于当前下账的金额
                        .setSql("balance = balance - " + new BigDecimal(accountUpDownDto.getAmount()))
                        .setSql("update_time = now()")
                );
                if (update == 0) {
                    throw new BusinessException(ResultEnum.ERROR.getCode(), "账户扣款失败，请检查账户余额信息");
                }
                List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo()));
                AccountVo result = accountVos.get(0);
                log.info("账户更新后的结果:{}", result);
                AccountFlowVo build = AccountFlowVo.builder()
                        .flowNo(accountUpDownDto.getFlowNo())
                        .outAccountNo(accountUpDownDto.getAccountNo())
                        .out_account_type(accountUpDownDto.getAccountType())
                        .bizType(accountUpDownDto.getBizType())
                        .funCode(accountUpDownDto.getFunCode())
                        .outStoreId(accountUpDownDto.getStoreId())
                        .amount(new BigDecimal(accountUpDownDto.getAmount()))
                        .bizOrderNo(accountUpDownDto.getBizOrderNo())
                        .bizOrderDate(accountUpDownDto.getBizOrderDate())
                        .bizOrderTime(accountUpDownDto.getBizOrderTime())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .remark(accountUpDownDto.getRemark())
                        .build();
                accountFlowMapper.insert(build);
                AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                        .storeId(accountUpDownDto.getStoreId())
                        .accountType(accountUpDownDto.getAccountType())
                        .flowDtlNo(TraceUtil.createTraceId())
                        .flowNo(accountUpDownDto.getFlowNo())
                        .amount(new BigDecimal(accountUpDownDto.getAmount()))
                        .curBalance(result.getBalance())
                        .bizType(accountUpDownDto.getBizType())
                        .fundDirection(BusiEnum.FUN_DIRECTION_D.getCode())
                        .funCode(accountUpDownDto.getFunCode())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .build();
                accountDtlMapper.insert(accountDtlVo);
                AccountUpDownVo accountUpDownVo = AccountUpDownVo.builder().accountNo(accountUpDownDto.getAccountNo())
                        .accountType(result.getAccountType())
                        .flowNo(accountUpDownDto.getFlowNo())
                        .funCode(accountUpDownDto.getFunCode())
                        .amount(new BigDecimal(accountUpDownDto.getAmount()))
                        .funDirection(BusiEnum.FUN_DIRECTION_D.getCode())
                        .bizType(accountUpDownDto.getBizType())
                        .channel_code(accountUpDownDto.getChannelCode())
                        .curBalance(result.getBalance())
                        .build();
                log.info("account down completed:{}", accountUpDownVo);
                return accountUpDownVo;

        } catch (Exception e) {
            log.error("transaction operation down banlance exception:{}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception {

        try {
                 List<String> accountNos = Stream.of(accountTransferDto.getOutAccountNo(), accountTransferDto.getInAccountNo())
                    .sorted().collect(Collectors.toList());
                //按照顺序加锁，防止死锁
                List<AccountVo> accounts = accountMapper.selectForUpdate(accountNos);
                AccountVo fromAccount = accounts.stream()
                        .filter(a -> a.getAccountNo().equals(accountTransferDto.getOutAccountNo())
                                && BusiEnum.NORMAL.getCode().equals(a.getAccountStatus()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ResultEnum.ERROR.getCode(), "扣款账户不存在"));

                AccountVo toAccount = accounts.stream()
                        .filter(a ->
                                a.getAccountNo().equals(accountTransferDto.getInAccountNo())
                                        && BusiEnum.NORMAL.getCode().equals(a.getAccountStatus())
                        )
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ResultEnum.ERROR.getCode(), "入账账户不存在"));
                int fromupdate = accountMapper.update(null, Wrappers.<AccountVo>lambdaUpdate(AccountVo.class)
                        .eq(AccountVo::getAccountNo, fromAccount.getAccountNo())
                        .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                        .ge(AccountVo::getBalance, accountTransferDto.getAmount())
                        .setSql("balance=balance - " + accountTransferDto.getAmount())
                        .setSql("update_time = now()"));
                if (fromupdate == 0) {
                    throw new BusinessException(ResultEnum.ERROR.getCode(), "转出方扣款异常");
                }
                int toupdate = accountMapper.update(null, Wrappers.lambdaUpdate(AccountVo.class)
                        .eq(AccountVo::getAccountNo, toAccount.getAccountNo())
                        .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                        .setSql("balance = balance + " + accountTransferDto.getAmount())
                        .setSql("update_time = now()"));
                if (toupdate == 0) {
                    throw new BusinessException(ResultEnum.ERROR.getCode(), "转入方上账异常");
                }
                log.info("account transfer update completed:{}", accountTransferDto);
                AccountFlowVo build = AccountFlowVo.builder()
                        .flowNo(accountTransferDto.getFlowNo())
                        .outAccountNo(accountTransferDto.getOutAccountNo())
                        .out_account_type(accountTransferDto.getOutAccountType())
                        .outStoreId(accountTransferDto.getOutStoreId())
                        .bizType(accountTransferDto.getBizType())
                        .funCode(accountTransferDto.getFunCode())
                        .inAccountNo(accountTransferDto.getInAccountNo())
                        .in_account_type(accountTransferDto.getInAccountType())
                        .inStoreId(accountTransferDto.getInStoreId())
                        .amount(new BigDecimal(accountTransferDto.getAmount()))
                        .bizOrderNo(accountTransferDto.getBizOrderNo())
                        .bizOrderDate(accountTransferDto.getBizOrderDate())
                        .bizOrderTime(accountTransferDto.getBizOrderTime())
                        .remark(accountTransferDto.getRemark())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .build();
                accountFlowMapper.insert(build);
                log.info("account transfer insert flow completed:{}", build);
                AccountDtlVo fromAccountDtlVo = AccountDtlVo.builder()
                        .storeId(accountTransferDto.getOutStoreId())
                        .accountType(accountTransferDto.getOutAccountType())
                        .flowDtlNo(TraceUtil.createTraceId())
                        .flowNo(accountTransferDto.getFlowNo())
                        .amount(new BigDecimal(accountTransferDto.getAmount()))
                        .curBalance(fromAccount.getBalance().subtract(new BigDecimal(accountTransferDto.getAmount())))
                        .bizType(accountTransferDto.getBizType())
                        .fundDirection(BusiEnum.FUN_DIRECTION_D.getCode())
                        .funCode(accountTransferDto.getFunCode())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .build();
                AccountDtlVo toAccountDtlVo = AccountDtlVo.builder()
                        .storeId(accountTransferDto.getInStoreId())
                        .accountType(accountTransferDto.getInAccountType())
                        .flowDtlNo(TraceUtil.createTraceId())
                        .flowNo(accountTransferDto.getFlowNo())
                        .amount(new BigDecimal(accountTransferDto.getAmount()))
                        .curBalance(fromAccount.getBalance().add(new BigDecimal(accountTransferDto.getAmount())))
                        .bizType(accountTransferDto.getBizType())
                        .fundDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                        .funCode(accountTransferDto.getFunCode())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .build();
                accountDtlMapper.batchInsert(Arrays.asList(fromAccountDtlVo, toAccountDtlVo));
                log.info("account transfer insert dtl completed:{}", build);
                AccountTransferVo result = AccountTransferVo.builder()
                        .inAccountNo(accountTransferDto.getInAccountNo())
                        .inAccountType(accountTransferDto.getInAccountType())
                        .inStoreId(accountTransferDto.getInStoreId())
                        .inCurBlance(toAccount.getBalance().add(new BigDecimal(accountTransferDto.getAmount())))
                        .outCurBlance(fromAccount.getBalance().subtract(new BigDecimal(accountTransferDto.getAmount())))
                        .outAccountNo(accountTransferDto.getOutAccountNo())
                        .outAccountType(accountTransferDto.getOutAccountType())
                        .outStoreId(accountTransferDto.getOutStoreId())
                        .bizType(accountTransferDto.getBizType())
                        .funCode(accountTransferDto.getFunCode())
                        .inAccountNo(accountTransferDto.getInAccountNo())
                        .outAccountNo(accountTransferDto.getOutAccountNo())
                        .outAccountType(accountTransferDto.getOutAccountType())
                        .outStoreId(accountTransferDto.getOutStoreId())
                        .bizOrderNo(accountTransferDto.getBizOrderNo())
                        .bizOrderDate(accountTransferDto.getBizOrderDate())
                        .bizOrderTime(accountTransferDto.getBizOrderTime())
                        .amount(accountTransferDto.getAmount())
                        .channelCode(accountTransferDto.getChannelCode())
                        .build();
                log.info("account transfer  completed:{}", result);
                return result;
        } catch (Exception e) {
            log.error("account operation transfer banlance exception:{}", e.getMessage());
            throw e;
        }
    }
}
