package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountFreezeRiskDto;
import com.al.account.bean.vo.AccountFreezeResultVo;
import com.al.account.bean.vo.AccountFreezeVo;
import com.al.account.bean.vo.AccountfreezeDetailVo;
import com.al.account.mapper.AccountFreezeDetailMapper;
import com.al.account.mapper.AccountFreezeMapper;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

@Service
@Slf4j
public class AccountFreezeTransaction {

    @Autowired
    private AccountFreezeMapper freezeMapper;
    @Autowired
    private AccountFreezeDetailMapper freezeDetailMapper;
    @Autowired
    private AccountTransactionImpl accountTransactionImpl;

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountFreezeResultVo riskFreeze(AccountFreezeRiskDto dto) throws Exception {
        AccountFreezeDto freezeDto = buildFreezeDto(dto, BusiEnum.FUNCODE_FREEZE.getCode());
        AccountFreezeResultVo result = accountTransactionImpl.freezeResultVo(freezeDto, true);

        String now = DateFormat.getDateTimeInstance().format(new Date());
        AccountFreezeVo freezeVo = AccountFreezeVo.builder()
                .freezeNo(dto.getFreezeNo())
                .acountNo(dto.getAccountNo())
                .merchantNo(dto.getMerchantNo())
                .accountType(dto.getAccountType())
                .bizOrder(dto.getBizOrderNo())
                .bizType(dto.getBizType())
                .funCode(BusiEnum.FUNCODE_FREEZE.getCode())
                .freezeStatus("0")
                .freezeTotalBalance(dto.getAmount())
                .frezeeBalance(dto.getAmount())
                .unfreezeBalance("0")
                .remark(dto.getRemark())
                .createTime(now)
                .updateTime(now)
                .build();
        freezeMapper.insert(freezeVo);

        AccountfreezeDetailVo detailVo = AccountfreezeDetailVo.builder()
                .freezeDtlNo(TraceUtil.createTraceId())
                .freezeNo(dto.getFreezeNo())
                .merchantNo(dto.getMerchantNo())
                .accountNo(dto.getAccountNo())
                .accountType(dto.getAccountType())
                .freezeBalance(dto.getAmount())
                .bizOrder(dto.getBizOrderNo())
                .bizType(dto.getBizType())
                .createTime(now)
                .updateTime(now)
                .build();
        freezeDetailMapper.insert(detailVo);
        return result;
    }

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountFreezeResultVo riskUnfreeze(AccountFreezeRiskDto dto) throws Exception {
        AccountFreezeVo freezeVo = freezeMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(AccountFreezeVo.class)
                        .eq(AccountFreezeVo::getFreezeNo, dto.getFreezeNo())
        );
        if (freezeVo == null) {
            throw new BusinessException("冻结单不存在");
        }
        if ("3".equals(freezeVo.getFreezeStatus())) {
            throw new BusinessException("冻结单已解冻完毕");
        }

        AccountFreezeDto freezeDto = buildFreezeDto(dto, BusiEnum.FUNCODE_UNFREEZE.getCode());
        AccountFreezeResultVo result = accountTransactionImpl.freezeResultVo(freezeDto, false);

        BigDecimal unfreezeAmount = new BigDecimal(dto.getAmount());
        BigDecimal alreadyUnfreeze = new BigDecimal(freezeVo.getUnfreezeBalance() == null ? "0" : freezeVo.getUnfreezeBalance());
        BigDecimal totalUnfreeze = alreadyUnfreeze.add(unfreezeAmount);
        BigDecimal freezeTotal = new BigDecimal(freezeVo.getFreezeTotalBalance() == null ? "0" : freezeVo.getFreezeTotalBalance());
        String status = totalUnfreeze.compareTo(freezeTotal) >= 0 ? "3" : "2";

        String now = DateFormat.getDateTimeInstance().format(new Date());
        freezeVo.setUnfreezeBalance(totalUnfreeze.toPlainString());
        freezeVo.setFreezeStatus(status);
        freezeVo.setUpdateTime(now);
        freezeMapper.update(freezeVo, com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate(AccountFreezeVo.class)
                .eq(AccountFreezeVo::getFreezeNo, dto.getFreezeNo()));

        AccountfreezeDetailVo detailVo = AccountfreezeDetailVo.builder()
                .freezeDtlNo(TraceUtil.createTraceId())
                .freezeNo(dto.getFreezeNo())
                .merchantNo(dto.getMerchantNo())
                .accountNo(dto.getAccountNo())
                .accountType(dto.getAccountType())
                .freezeBalance("-" + dto.getAmount())
                .bizOrder(dto.getBizOrderNo())
                .bizType(dto.getBizType())
                .createTime(now)
                .updateTime(now)
                .build();
        freezeDetailMapper.insert(detailVo);
        return result;
    }

    private AccountFreezeDto buildFreezeDto(AccountFreezeRiskDto dto, String funCode) {
        return AccountFreezeDto.builder()
                .flowNo(dto.getFlowNo())
                .accountNo(dto.getAccountNo())
                .merchantNo(dto.getMerchantNo())
                .accountType(dto.getAccountType())
                .channelCode(dto.getChannelCode())
                .bizType(dto.getBizType())
                .bizOrderNo(dto.getBizOrderNo())
                .bizOrderDate(dto.getBizOrderDate())
                .bizOrderTime(dto.getBizOrderTime())
                .amount(dto.getAmount())
                .funCode(funCode)
                .remark(dto.getRemark())
                .build();
    }
}
