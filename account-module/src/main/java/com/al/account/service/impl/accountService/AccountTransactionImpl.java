package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
@Service
@Slf4j
public class AccountTransactionImpl {
    @Autowired
    private AccountOpenMapper accountOpenMapper;
    @Autowired
    private AccountMapper accountMapper;
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
}
