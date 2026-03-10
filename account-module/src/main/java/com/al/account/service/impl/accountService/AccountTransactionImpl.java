package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.dto.AccountFreezeDto;
import com.al.account.bean.dto.AccountTransferDto;
import com.al.account.bean.dto.AccountUpDownDto;
import com.al.account.bean.vo.*;
import com.al.account.mapper.AccountDtlMapper;
import com.al.account.mapper.AccountFlowMapper;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.common.business.BusiEnum;
import com.al.common.business.FeeTypeEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.C;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountOpenVo save(AccountDto accountDto) throws Exception {
        try {
            log.info("start open account infomation save:{}", accountDto);
            AccountVo build = AccountVo.builder()
                    .accountNo(accountDto.getAccountNo())
                    .merchantNo(accountDto.getMerchantNo())
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
                    .merchantNo(accountDto.getMerchantNo())
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
                    .merchantNo(accountDto.getMerchantNo())
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
                .merchantNo(accountDto.getMerchantNo())
                .accountNo(accountDto.getAccountNo())
                .updateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT))).build();

        int update = accountMapper.update(build, Wrappers.lambdaUpdate(AccountVo.class)
                .eq(AccountVo::getAccountNo, accountDto.getAccountNo())
                .eq(AccountVo::getMerchantNo, accountDto.getAccountNo())
                .eq(AccountVo::getChannelCode, accountDto.getChannelCode()));
        if (update == 0) {
            return "更新失败";
        }
        AccountOpenFlowVo accountFlow = AccountOpenFlowVo.builder().accountNo(accountDto.getAccountNo())
                .accountType(accountDto.getAccountType())
                .accountNo(accountDto.getAccountNo())
                .merchantNo(accountDto.getAccountNo())
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
    public AccountUpDownVo upDown(AccountUpDownDto accountUpDownDto) throws Exception {
        try {
            // // 1 更新余额
            updateAccount(accountUpDownDto);
            log.info("account up down already completed");
            // 2 查询最新账户
            AccountVo account = getAccount(accountUpDownDto.getAccountNo());
            log.info("账户更新后的结果:{}", account);
            // 3 构建流水
            AccountFlowVo accountFlowVo = buildFlow(accountUpDownDto);
            // 4 构建明细
            List<AccountDtlVo> accountDtlVos = buildDtlList(accountUpDownDto, account);
            accountFlowMapper.insert(accountFlowVo);
            accountDtlVos.forEach(accountDtlMapper::insert);
            log.info("insert  account flow and dtl data completed:{}", account);
            //组装返回数据
            return buildResult(accountUpDownDto,account);
        } catch (Exception e) {
            log.error("transaction operation down up banlance exception:{}", e.getMessage());
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "流水号重复");
            } else {
                throw e;
            }
        }
    }

    /**
     * 组装上下账返回数据
     * @param accountUpDownDto
     * @param accountVo
     * @return
     */
    private AccountUpDownVo buildResult(AccountUpDownDto accountUpDownDto, AccountVo accountVo) {

        AccountUpDownVo accountUpDownVo = AccountUpDownVo.builder().accountNo(accountUpDownDto.getAccountNo())
                .accountType(accountVo.getAccountType())
                .flowNo(accountUpDownDto.getFlowNo())
                .funCode(accountUpDownDto.getFunCode())
                .amount(new BigDecimal(accountUpDownDto.getAmount()))
                .bizType(accountUpDownDto.getBizType())
                .channel_code(accountUpDownDto.getChannelCode())
                .curBalance(accountVo.getBalance())
                .feeType(accountUpDownDto.getFeeType())
                .feeAmount(accountUpDownDto.getFeeAmount())
                .build();
        if(isUp(accountUpDownDto.getFunCode())) {
            accountUpDownVo.setFunDirection(BusiEnum.FUN_DIRECTION_C.getCode());
        } else {
            accountUpDownVo.setFunDirection(BusiEnum.FUN_DIRECTION_D.getCode());
        }
        log.info("assesbly return data completed:{}", accountUpDownVo);
        return accountUpDownVo;
    }

    /**
     * 获取账户信息
     * @param accountNo
     * @return
     */
    private AccountVo getAccount(String accountNo) {
        return accountMapper.selectOne(
                Wrappers.lambdaQuery(AccountVo.class)
                        .eq(AccountVo::getAccountNo, accountNo)
        );
    }
    /**
     * 更新账户余额信息
     * @param accountUpDownDto
     * @throws Exception
     */
    public void updateAccount(AccountUpDownDto accountUpDownDto) throws Exception{
        log.info("account up down balance infomation check completed:{}", accountUpDownDto);
        int rows = accountMapper.update(
                null,
                Wrappers.lambdaUpdate(AccountVo.class)
                        .eq(AccountVo::getAccountNo, accountUpDownDto.getAccountNo())
                        .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                        .eq(AccountVo::getMerchantNo, accountUpDownDto.getMerchantNo())
                        .eq(AccountVo::getAccountType, accountUpDownDto.getAccountType())
                        .apply(BusiEnum.FUNCODE_DOWN.getCode().equals(accountUpDownDto.getFunCode() )|| BusiEnum.FUNCODE_DOWNWAY.getCode().equals(accountUpDownDto.getFunCode()),"balance - frozen_balance >= {0}", accountUpDownDto.getAmount())//下账
                        .ge(BusiEnum.FUNCODE_TRANSIT_DOWN.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode()),AccountVo::getTransitBalance, accountUpDownDto.getAmount())//在途下账判断
                        .setSql(FeeTypeEnum.INTERNAL_BUCKLE.getCode().equals(accountUpDownDto.getFeeType()) && (BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())),"balance = balance + " + (new BigDecimal(accountUpDownDto.getAmount()).subtract(accountUpDownDto.getFeeAmount())))//内扣上账
                        .setSql(!FeeTypeEnum.INTERNAL_BUCKLE.getCode().equals(accountUpDownDto.getFeeType()) && (BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())),"balance = balance + " + new BigDecimal(accountUpDownDto.getAmount()))//其他上账
                        .setSql((BusiEnum.FUNCODE_DOWN.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_DOWNWAY.getCode().equals(accountUpDownDto.getFunCode())),"balance = balance - " + new BigDecimal(accountUpDownDto.getAmount()))//其他下账
                        .setSql(BusiEnum.FUNCODE_DOWNWAY.getCode().equals(accountUpDownDto.getFunCode()), "transit_balance   = transit_balance  + " + new BigDecimal(accountUpDownDto.getAmount()))//上账到在途
                        .setSql(BusiEnum.FUNCODE_TRANSIT_DOWN.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode()), "transit_balance   = transit_balance  - " + new BigDecimal(accountUpDownDto.getAmount()))//上账到在途
                        .setSql("update_time = now()")
        );
        if (rows == 0) {
            if(BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode())){
                throw new BusinessException("账户上账失败，请检查账户信息");
            }else if (BusiEnum.FUNCODE_TRANSIT_DOWN.getCode().equals(accountUpDownDto.getFunCode()) || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())){
                throw new BusinessException("在途账户余额不足");
            }else{
                throw new BusinessException("账户可用余额不足");
            }
        }
    }

    /**
     * 组装流水信息
     * @param accountUpDownDto
     */
    private  AccountFlowVo buildFlow(AccountUpDownDto accountUpDownDto){
        AccountFlowVo build = AccountFlowVo.builder()
                .flowNo(accountUpDownDto.getFlowNo())
                .bizType(accountUpDownDto.getBizType())
                .funCode(accountUpDownDto.getFunCode())
                .amount(new BigDecimal(accountUpDownDto.getAmount()))
                .bizOrderNo(accountUpDownDto.getBizOrderNo())
                .bizOrderDate(accountUpDownDto.getBizOrderDate())
                .bizOrderTime(accountUpDownDto.getBizOrderTime())
                .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                .remark(accountUpDownDto.getRemark())
                .feeType(accountUpDownDto.getFeeType())
                .feeAmount(accountUpDownDto.getFeeAmount())
                .build();
        if(isUp(accountUpDownDto.getFunCode())){
            build.setInAccountNo(accountUpDownDto.getAccountNo());
            build.setInMerchantNo(accountUpDownDto.getMerchantNo());
            build.setIn_account_type(accountUpDownDto.getAccountType());
        }else{
            build.setOutAccountNo(accountUpDownDto.getAccountNo());
            build.setOutMerchantNo(accountUpDownDto.getMerchantNo());
            build.setOut_account_type(accountUpDownDto.getAccountType());
        }
        log.info("account build account flow data:{}", build);
       return build;
    }

    /**
     * 判断是否是上账操作
     * @param funCode
     * @return
     */
    private boolean isUp(String funCode) {
        return BusiEnum.FUNCODE_UP.getCode().equals(funCode)
                || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(funCode);
    }

    /**
     * 判断是否是下账操作
     * @param funCode
     * @return
     */
    private boolean isDown(String funCode) {
        return BusiEnum.FUNCODE_DOWN.getCode().equals(funCode)
                || BusiEnum.FUNCODE_DOWNWAY.getCode().equals(funCode)
                || BusiEnum.FUNCODE_TRANSIT_DOWN.getCode().equals(funCode);
    }

    /**
     * 组装上下账明细数据
     * @param accountUpDownDto
     * @param result
     * @return
     */
    public List<AccountDtlVo> buildDtlList(AccountUpDownDto accountUpDownDto, AccountVo result){
        List<AccountDtlVo> dtlList = new ArrayList<>();
        if (FeeTypeEnum.INTERNAL_BUCKLE.getCode().equals(accountUpDownDto.getFeeType())) {
            AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                    .merchantNo(accountUpDownDto.getMerchantNo())
                    .accountType(accountUpDownDto.getAccountType())
                    .flowDtlNo(TraceUtil.createTraceId())
                    .flowNo(accountUpDownDto.getFlowNo())
                    .amount(new BigDecimal(accountUpDownDto.getAmount()))
                    .fundDirection(BusiEnum.FUN_DIRECTION_C.getCode())
                    .bizType(accountUpDownDto.getBizType())
                    .funCode(accountUpDownDto.getFunCode())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
            if(BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode())
                    || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())){
                accountDtlVo.setFundDirection(BusiEnum.FUN_DIRECTION_C.getCode());
                accountDtlVo.setAmount(new BigDecimal(accountUpDownDto.getAmount()));
                accountDtlVo.setCurBalance(result.getBalance().add(accountUpDownDto.getFeeAmount()));
            }else{
                accountDtlVo.setFundDirection(BusiEnum.FUN_DIRECTION_D.getCode());
                accountDtlVo.setAmount(new BigDecimal(accountUpDownDto.getAmount()).subtract(accountUpDownDto.getFeeAmount()));
                accountDtlVo.setCurBalance(result.getBalance().add(accountUpDownDto.getFeeAmount()));
            }
            dtlList.add(accountDtlVo);
            //内扣手续费扣除
            AccountDtlVo feeVo = AccountDtlVo.builder()
                    .merchantNo(accountUpDownDto.getMerchantNo())
                    .accountType(accountUpDownDto.getAccountType())
                    .flowDtlNo(TraceUtil.createTraceId())
                    .flowNo(accountUpDownDto.getFlowNo())
                    .amount(accountUpDownDto.getFeeAmount())
                    .curBalance(result.getBalance())
                    .fundDirection(BusiEnum.FUN_DIRECTION_D.getCode())
                    .bizType(accountUpDownDto.getBizType())
                    .funCode(accountUpDownDto.getFunCode())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
            if(BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode())
                    || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())){
                feeVo.setFundDirection(BusiEnum.FUN_DIRECTION_D.getCode());
            }else{
                feeVo.setFundDirection(BusiEnum.FUN_DIRECTION_D.getCode());
            }
            dtlList.add(feeVo);
            log.info("account build account dtl detail data:{}", dtlList);

        }else{
            AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                    .merchantNo(accountUpDownDto.getMerchantNo())
                    .accountType(accountUpDownDto.getAccountType())
                    .flowDtlNo(TraceUtil.createTraceId())
                    .flowNo(accountUpDownDto.getFlowNo())
                    .amount(new BigDecimal(accountUpDownDto.getAmount()))
                    .curBalance(result.getBalance())
                    .bizType(accountUpDownDto.getBizType())
                    .funCode(accountUpDownDto.getFunCode())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
            if(BusiEnum.FUNCODE_UP.getCode().equals(accountUpDownDto.getFunCode())
                    || BusiEnum.FUNCODE_TRANSIT_UP.getCode().equals(accountUpDownDto.getFunCode())){
                accountDtlVo.setFundDirection(BusiEnum.FUN_DIRECTION_C.getCode());
            }else{
                accountDtlVo.setFundDirection(BusiEnum.FUN_DIRECTION_D.getCode());
            }
            dtlList.add(accountDtlVo);
            log.info("account build account dtl detail data:{}", dtlList);
        }
        return dtlList;

    }
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public AccountTransferVo transfer(AccountTransferDto accountTransferDto) throws Exception {
        try {
                log.info("start account transfer incoming data:{}", accountTransferDto);
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
                        .apply("balance - frozen_balance >= {0}", accountTransferDto.getAmount())
                        .setSql("balance=balance - " + accountTransferDto.getAmount())
                        .setSql("update_time = now()"));
                if (fromupdate == 0) {
                    throw new BusinessException(ResultEnum.ERROR.getCode(), "转出方扣款可用余额不足异常");
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
                        .outMerchantNo(accountTransferDto.getOutMerchantNo())
                        .bizType(accountTransferDto.getBizType())
                        .funCode(accountTransferDto.getFunCode())
                        .inAccountNo(accountTransferDto.getInAccountNo())
                        .in_account_type(accountTransferDto.getInAccountType())
                        .inMerchantNo(accountTransferDto.getInMerchantNo())
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
                        .merchantNo(accountTransferDto.getOutMerchantNo())
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
                        .merchantNo(accountTransferDto.getInMerchantNo())
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
                        .inMerchantNo(accountTransferDto.getInMerchantNo())
                        .inCurBlance(toAccount.getBalance().add(new BigDecimal(accountTransferDto.getAmount())))
                        .outCurBlance(fromAccount.getBalance().subtract(new BigDecimal(accountTransferDto.getAmount())))
                        .outAccountNo(accountTransferDto.getOutAccountNo())
                        .outAccountType(accountTransferDto.getOutAccountType())
                        .outMerchantNo(accountTransferDto.getOutMerchantNo())
                        .bizType(accountTransferDto.getBizType())
                        .funCode(accountTransferDto.getFunCode())
                        .inAccountNo(accountTransferDto.getInAccountNo())
                        .outAccountNo(accountTransferDto.getOutAccountNo())
                        .outAccountType(accountTransferDto.getOutAccountType())
                        .outMerchantNo(accountTransferDto.getOutMerchantNo())
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
    @Transactional(rollbackFor = Exception.class,timeout = 30)
    public AccountFreezeResultVo freezeResultVo(AccountFreezeDto freezeDto,boolean flag) throws Exception {
        try {
            log.info("start account  freeze operation:{}", freezeDto);
            int update = accountMapper.update(null, Wrappers.lambdaUpdate(AccountVo.class)
                    .eq(AccountVo::getAccountNo, freezeDto.getAccountNo())
                    .eq(AccountVo::getAccountType, freezeDto.getAccountType())
                    .eq(AccountVo::getMerchantNo, freezeDto.getMerchantNo())
                    .eq(AccountVo::getAccountStatus, BusiEnum.NORMAL.getCode())
                    .apply(flag,"balance - frozen_balance >= {0}", freezeDto.getAmount())//冻结
                    .ge(!flag,AccountVo::getFrozenBalance,freezeDto.getAmount())//解冻
                    .setSql(flag,"frozen_balance=frozen_balance + " + new BigDecimal(freezeDto.getAmount()))//冻结
                    .setSql(!flag,"frozen_balance=frozen_balance-"+ new BigDecimal(freezeDto.getAmount()))//解冻
                    .setSql("update_time=now()"));
            if (update == 0) {
                log.info("账户冻结解冻操作失败:{}", freezeDto);
                if (flag) {
                    throw new BusinessException("账户冻结余额不足");
                }else{
                    throw new BusinessException("账户解冻余额不足");
                }
            }
            AccountVo accountVo = accountMapper.selectOne(Wrappers.<AccountVo>lambdaQuery(AccountVo.class)
                    .eq(AccountVo::getAccountNo, freezeDto.getAccountNo()));
            log.info("account freeze after infomation:{}", accountVo);
            AccountFlowVo build = AccountFlowVo.builder()
                    .flowNo(freezeDto.getFlowNo())
                    .bizType(freezeDto.getBizType())
                    .funCode(freezeDto.getFunCode())
                    .inAccountNo(freezeDto.getAccountNo())
                    .in_account_type(freezeDto.getAccountType())
                    .inMerchantNo(freezeDto.getMerchantNo())
                    .amount(new BigDecimal(freezeDto.getAmount()))
                    .bizOrderNo(freezeDto.getBizOrderNo())
                    .bizOrderDate(freezeDto.getBizOrderDate())
                    .bizOrderTime(freezeDto.getBizOrderTime())
                    .remark(freezeDto.getRemark())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .build();
            accountFlowMapper.insert(build);
            AccountDtlVo accountDtlVo = AccountDtlVo.builder()
                    .merchantNo(freezeDto.getMerchantNo())
                    .accountType(freezeDto.getAccountType())
                    .flowDtlNo(TraceUtil.createTraceId())
                    .flowNo(freezeDto.getFlowNo())
                    .amount(new BigDecimal(freezeDto.getAmount()))
                    .curBalance(accountVo.getBalance())
                    .bizType(freezeDto.getBizType())
                    .fundDirection(flag ? BusiEnum.FUN_DIRECTION_F.getCode() : BusiEnum.FUN_DIRECTION_U.getCode())
                    .funCode(freezeDto.getFunCode())
                    .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
            accountDtlMapper.insert(accountDtlVo);
            return AccountFreezeResultVo.builder()
                    .freezeAmount(new BigDecimal(freezeDto.getAmount()))
                    .accountNo(freezeDto.getAccountNo())
                    .accountType(freezeDto.getAccountType())
                    .merchantNo(freezeDto.getMerchantNo())
                    .bizType(freezeDto.getBizType())
                    .funCode(freezeDto.getFunCode())
                    .frozenBalance(accountVo.getFrozenBalance())
                    .flowNo(freezeDto.getFlowNo())
                    .accountType(freezeDto.getAccountType())
                    .channel_code(freezeDto.getChannelCode())
                    .curBalance(accountVo.getBalance())
                    .build();
        }catch (Exception e){
            log.error("account operation  freeze or unfreeze exception message:{}", e.getMessage());
            throw e;
        }
    }
}
