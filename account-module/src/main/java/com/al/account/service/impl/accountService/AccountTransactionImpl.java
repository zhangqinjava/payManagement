package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountUpDto;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountDtlMapper;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.common.business.BusiEnum;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    @Transactional(rollbackFor = Exception.class,timeout = 30)
    public String save(AccountDto accountDto) throws Exception{
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
                    .openOrderNo(accountDto.getFlow())
                    .currency(BusiEnum.RMB.getCode())
                    .channelCode(accountDto.getChannelCode())
                    .channelAccountNo(accountDto.getChannelAccountNo())
                    .operator(accountDto.getOperation()==null?"system":accountDto.getOperation())
                    .openStatus(BusiEnum.NORMAL.getCode())
                    .modifyUser(accountDto.getModifyUser()==null?"system":accountDto.getModifyUser())
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date())).build();
            accountMapper.insert(build);
            accountOpenMapper.insert(accountFlow);
            return "开户成功";
        }catch (Exception e){
            log.error("open save account information error:{}", e.getMessage());
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "重复开户");
            }else if (e instanceof BusinessException) {
                throw e;
            }else{
                throw new BusinessException(ResultEnum.ERROR.getCode(), "开户失败");
            }
        }
    }
    @Transactional(rollbackFor = Exception.class,timeout = 30)
    public String update(AccountDto accountDto) throws Exception{
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
                .openOrderNo(accountDto.getFlow())
                .currency(BusiEnum.RMB.getCode())
                .channelCode(accountDto.getChannelCode())
                .channelAccountNo(accountDto.getChannelAccountNo())
                .operator(accountDto.getOperation()==null?"system":accountDto.getOperation())
                .openStatus(accountDto.getAccountStatus())
                .modifyUser(accountDto.getModifyUser()==null?"system":accountDto.getModifyUser())
                .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                .updateTime(DateFormat.getDateTimeInstance().format(new Date())).build();
        accountOpenMapper.insert(accountFlow);
        return "更新成功";
    }
    @Transactional(rollbackFor = Exception.class,timeout = 30)
    public AccountUpDownVo up(AccountUpDto accountUpDto) throws Exception{
        RLock lock = redissonClient.getLock("account_banlance_lock" + accountUpDto.getFlowNo());
        try{
            if(lock.tryLock(10, TimeUnit.SECONDS)) {
                int rows = accountMapper.update(
                        null,
                        Wrappers.lambdaUpdate(AccountVo.class)
                                .eq(AccountVo::getAccountNo, accountUpDto.getAccountNo())
                                // 方式一：直接拼接字符串 (数值类型是安全的)
                                .setSql("balance = balance + " + new BigDecimal(accountUpDto.getAmount()))
                                .setSql("update_time = now()")
                );
                if (rows == 0) {
                    throw new BusinessException(ResultEnum.ERROR.getCode(), "数据库更新失败");
                }
                List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountUpDto.getAccountNo()));
                AccountVo result = accountVos.get(0);
                log.info("账户更新后的结果:{}", result);
                AccountFlowVo build = AccountFlowVo.builder()
                        .flowNo(accountUpDto.getFlowNo())
                        .inAccountNo(accountUpDto.getAccountNo())
                        .bizType(accountUpDto.getBizType())
                        .funCode(accountUpDto.getFunCode())
                        .inStoreId(accountUpDto.getStoreId())
                        .amount(new BigDecimal(accountUpDto.getAmount()))
                        .bizOrderNo(accountUpDto.getBizOrderNo())
                        .bizOrderDate(accountUpDto.getBizOrderDate())
                        .bizOrderTime(accountUpDto.getBizOrderTime())
                        .remark(accountUpDto.getRemark())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .build();
                accountFlowMapper.insert(build);
                AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                        .storeId(accountUpDto.getStoreId())
                        .flowDtlNo(TraceUtil.createTraceId())
                        .flowNo(accountUpDto.getFlowNo())
                        .amount(new BigDecimal(accountUpDto.getAmount()))
                        .curBalance(result.getBalance())
                        .bizType(accountUpDto.getBizType())
                        .fundDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                        .funCode(accountUpDto.getFunCode())
                        .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .build();
                accountDtlMapper.insert(accountDtlVo);
                AccountUpDownVo accountUpDownVo = AccountUpDownVo.builder().accountNo(accountUpDto.getAccountNo())
                        .accountType(result.getAccountType())
                        .flowNo(accountUpDto.getFlowNo())
                        .funCode(accountUpDto.getFunCode())
                        .amount(new BigDecimal(accountUpDto.getAmount()))
                        .funDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                        .bizType(accountUpDto.getBizType())
                        .channel_code(accountUpDto.getChannelCode())
                        .curBalance(result.getBalance())
                        .build();
                log.info("account up completed:{}", accountUpDownVo);
                return accountUpDownVo;
            }else{
                throw new BusinessException(ResultEnum.ERROR.getCode(), "系统繁忙，请稍后再试");
            }
        }catch (Exception e){
            log.error("transaction operation up banlance exception:{}",e.getMessage());
            if(e instanceof DuplicateKeyException){
                throw new BusinessException(ResultEnum.ERROR.getCode(),"流水号重复");
            }else{
                throw e;
            }
        }finally {
            lock.unlock();
        }
    }
}
