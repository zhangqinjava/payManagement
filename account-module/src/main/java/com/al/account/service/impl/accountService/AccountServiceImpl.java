package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.account.service.accountService.AccountService;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mysql.cj.util.StringUtils;
import io.reactivex.rxjava3.core.Completable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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
    private AccountMapper accountMapper;
    @Resource(name = "accountThreadPool")
    private Executor accountThreadPool;
    @Override
    public String save(AccountDto accountDto) throws Exception{
        try {
            log.info("start open account infomation save:{}", accountDto);
            checkParam(accountDto);
            Long count = accountOpenMapper.selectCount(Wrappers.lambdaQuery(AccountOpenFlowVo.class)
                    .eq(AccountOpenFlowVo::getOpenOrderNo, accountDto.getFlow()));
            if (count > 0) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "请求流水号重复");
            }
            String traceId = TraceUtil.createTraceId();
            accountDto.setAccountNo(traceId);
            accountTransactionImpl.save(accountDto);
            return "开户成功";
        }catch (Exception e){
            log.error("open save account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String update(AccountDto accountDto) throws Exception {
        try {
            log.info("start open account information update:{}", accountDto);
            checkParam(accountDto);
            CompletableFuture<String> validateFuture =
                    CompletableFuture.supplyAsync(() ->
                            accountMapper.selectCount(
                                    Wrappers.lambdaQuery(AccountVo.class)
                                            .eq(AccountVo::getAccountNo, accountDto.getAccountNo())
                            ), accountThreadPool
                    ).thenCombine(
                            CompletableFuture.supplyAsync(() ->
                                    accountOpenMapper.selectCount(
                                            Wrappers.lambdaQuery(AccountOpenFlowVo.class)
                                                    .eq(AccountOpenFlowVo::getOpenOrderNo, accountDto.getFlow())
                                    ), accountThreadPool
                            ),
                            (count, flowCount) -> {
                                if (count == 0) {
                                    return "账户信息不存在";
                                }
                                if (flowCount > 0) {
                                    return "流水号重复";
                                }
                                return "校验通过";
                            }
                    );
            // 等待并触发异常
            String join = validateFuture.join();
            if (!"校验通过".equals(join)) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), join);
            }
            return accountTransactionImpl.update(accountDto);
        }catch (Exception e){
            log.error("open update account information error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(AccountDto accountDto)throws Exception {
        try {
            log.info("start open account information delete:{}", accountDto);
            int delete = accountMapper.delete(Wrappers.lambdaQuery(AccountVo.class).eq(AccountVo::getAccountNo, accountDto.getAccountNo()));
            if (delete == 0) {
                throw new BusinessException(ResultEnum.ERROR.getCode(), "账户号不存在");
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
                    .eq(AccountVo::getStoreId, accountDto.getStoreId())
                     .eq(AccountVo::getAccountType, accountDto.getAccountType()));
            return accountVos;
        }catch (Exception e){
            log.error("open query account information error:{}", e.getMessage());
            throw e;
        }
        }
    public void checkParam(AccountDto accountDto) throws Exception{
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountType()) &&!BusiEnum.contains(accountDto.getAccountType())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户类型不正确");
        }
        if (!StringUtils.isNullOrEmpty(accountDto.getAccountStatus()) && !BusiEnum.contains(accountDto.getAccountStatus())) {
            throw new BusinessException(ResultEnum.ERROR.getCode(),"账户状态不正确");
        }
    }
}
