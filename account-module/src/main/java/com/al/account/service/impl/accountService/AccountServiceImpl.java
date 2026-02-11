package com.al.account.service.impl.accountService;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountOpenFlowVo;
import com.al.account.bean.vo.AccountOpenVo;
import com.al.account.bean.vo.AccountVo;
import com.al.account.mapper.AccountMapper;
import com.al.account.mapper.AccountOpenMapper;
import com.al.account.service.accountService.AccountService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.al.common.result.ResultEnum;
import com.al.common.util.TraceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
            accountDto.setAccountNo(traceId);
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
                                    .eq(AccountVo::getStoreId, accountDto.getStoreId())
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
