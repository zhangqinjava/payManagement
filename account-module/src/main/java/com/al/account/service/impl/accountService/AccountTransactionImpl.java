package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountUpDto;
import com.al.account.bean.vo.AccountFlowVo;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountUpVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

@Service
@Slf4j
public class AccountTransactionImpl {
    @Autowired
    private AccountOpenMapper accountOpenMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountFlowMapper accountFlowMapper;

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
                    .availableBalance(BigDecimal.ZERO)
                    .currency(BusiEnum.RMB.getCode())
                    .frozenBalance(BigDecimal.ZERO)
                    .availableBalance(BigDecimal.ZERO)
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
    public  AccountUpVo up( AccountUpDto accountUpDto,AccountVo accountVo) throws Exception{
        try{
            int rows = accountMapper.update(
                    null,
                    Wrappers.lambdaUpdate(AccountVo.class)
                            .eq(AccountVo::getAccountNo, accountUpDto.getAccountNo())
                            // 方式一：直接拼接字符串 (数值类型是安全的)
                            .setSql("balance = balance + " + accountUpDto.getAmount())
                            .setSql("available_balance = available_balance + " + accountUpDto.getAmount())
            );
            if(rows==0){
                throw new BusinessException(ResultEnum.ERROR.getCode(), "数据库更新失败");
            }
            List<AccountVo> accountVos = accountMapper.selectList(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountUpDto.getAccountNo()));
            AccountVo result = accountVos.get(0);
            log.info("账户更新后的结果:{}",result);
            AccountFlowVo build = AccountFlowVo.builder()
                    .flowNo(accountUpDto.getFlowNo())
                    .curBalance(result.getBalance())
                    .bizType(accountUpDto.getBizType())
                    .fundDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                    .funCode(accountUpDto.getFunCode())
                    .storeId(accountUpDto.getStoreId())
                    .amount(accountUpDto.getAmount())
                    .bizOrderNo(accountUpDto.getBizOrderNo())
                    .createTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT)))
                    .updateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT)))
                    .build();
            accountFlowMapper.insert(build);
            AccountUpVo accountUpVo = AccountUpVo.builder().accountNo(accountUpDto.getAccountNo())
                    .accountType(result.getAccountType())
                    .funCode(accountUpDto.getFunCode())
                    .amount(accountUpDto.getAmount())
                    .bizType(accountUpDto.getBizType())
                    .channel_code(accountUpDto.getChannelCode())
                    .curAmount(result.getBalance()).build();
            return accountUpVo;
        }catch (Exception e){
            log.error("transaction operation up banlance exception:{}",e.getMessage());
            if(e instanceof DuplicateKeyException){
                throw new BusinessException(ResultEnum.ERROR.getCode(),"流水号重复");
            }else{
                throw e;
            }
        }

    }
}
