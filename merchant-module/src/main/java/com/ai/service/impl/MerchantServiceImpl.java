package com.ai.service.impl;

import com.ai.bean.dto.MerchantDto;
import com.ai.bean.vo.MerchantVo;
import com.ai.config.GenericCache;
import com.ai.mapper.MerchantMapper;
import com.ai.service.MerchantService;
import com.al.common.business.Const;
import com.al.common.business.MerchantEnum;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private GenericCache genericCache;
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private MerchantMapper merchantMapper;
    @Resource(name = "accountThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantVo save(MerchantDto merchantDto) throws Exception {
        try {
            log.info("save merchant information start:{} ", merchantDto);
            MerchantVo build = MerchantVo.builder()
                    .merchantId(merchantDto.getMerchantId())
                    .merchantName(merchantDto.getMerchantName())
                    .merchantType(merchantDto.getMerchantType())
                    .status(MerchantEnum.NOMAL.getCode())
                    .contactName(merchantDto.getContactName())
                    .contactPhone(merchantDto.getContactPhone())
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .build();
            merchantMapper.insert(build);
            return build;
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                log.info(" merchantId information 主键冲突，需要重新生成:{} ", e.getMessage());
                MerchantVo build = MerchantVo.builder()
                        .merchantId(merchantDto.getMerchantId())
                        .merchantName(merchantDto.getMerchantName())
                        .merchantType(merchantDto.getMerchantType())
                        .status(MerchantEnum.NOMAL.getCode())
                        .contactName(merchantDto.getContactName())
                        .contactPhone(merchantDto.getContactPhone())
                        .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                        .build();
                merchantMapper.insert(build);
                return build;
            }else{
                log.info("save merchant information fail:{} ", e.getMessage());
                throw new BusinessException("保存失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantVo update(MerchantDto merchantDto) throws Exception{
        try{
            log.info("update merchant information start:{} ", merchantDto);
            MerchantVo build = MerchantVo.builder()
                    .merchantId(merchantDto.getMerchantId())
                    .merchantName(merchantDto.getMerchantName())
                    .merchantType(merchantDto.getMerchantType())
                    .status(merchantDto.getStatus())
                    .contactName(merchantDto.getContactName())
                    .contactPhone(merchantDto.getContactPhone())
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .build();
            merchantMapper.update(build,Wrappers.lambdaUpdate(MerchantVo.class).eq(MerchantVo::getMerchantId, merchantDto.getMerchantId()));
            // 2. 删除缓存（让下一次读回源）
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            genericCache.delete(Const.MERCHANT_PREFIX+merchantDto.getMerchantId());

                            // 延迟双删
                            CompletableFuture.runAsync(() -> {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {}
                                genericCache.delete(Const.MERCHANT_PREFIX+merchantDto.getMerchantId());
                            },threadPoolExecutor);
                        }
                    }
            );
            return build;
        }catch (Exception e){
            log.error("update merchant information fail:{} ", e.getMessage());
            throw new BusinessException("更新失败" );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(String merchantId) throws Exception{
        try {
            if (StringUtils.isBlank(merchantId)) {
                throw new BusinessException("商户号不能为空");
            }
            merchantMapper.delete(Wrappers.lambdaUpdate(MerchantVo.class).eq(MerchantVo::getMerchantId, merchantId));
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            genericCache.delete(Const.MERCHANT_PREFIX+merchantId);

                            // 延迟双删
                            CompletableFuture.runAsync(() -> {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {}
                                genericCache.delete(Const.MERCHANT_PREFIX+merchantId);
                            },threadPoolExecutor);
                        }
                    }
            );
            return "删除成功";
        }catch (Exception e){
            log.error("delete merchant information fail:{} ", e.getMessage());
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public MerchantVo query(String merchantId) throws Exception{
            if(StringUtils.isEmpty(merchantId)){
                throw  new BusinessException("商户号不能为空!");
            }
            // 1. 先查缓存
            MerchantVo merchant = genericCache.get(Const.MERCHANT_PREFIX+merchantId,MerchantVo.class);
            if (merchant != null) {
                return merchant;
            }
            // 2. 空值缓存判断
            if (genericCache.isNullCached(Const.MERCHANT_PREFIX+merchantId)) {
                return null;
            }
            RLock lock = redissonClient.getLock(Const.MERCHANT_LOCK+merchantId);
            try {
                // 3. 防击穿加锁
                if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                    // double check
                    merchant = genericCache.get(Const.MERCHANT_PREFIX+merchantId,MerchantVo.class);
                    if (merchant != null) {
                        return merchant;
                    }
                    // 4. DB 回源
                     merchant = merchantMapper.selectOne(Wrappers.lambdaQuery(MerchantVo.class).eq(MerchantVo::getMerchantId, merchantId));
                    if (merchant == null) {
                        genericCache.cacheNull(Const.MERCHANT_NULL_PREFIX,merchantId);
                        return null;
                    }
                    // 5. 写缓存
                    genericCache.set(Const.MERCHANT_PREFIX+merchant.getMerchantId(),merchant);
                    return merchant;
                }else {
                    // 没拿到锁，短暂 sleep 后再读缓存
                    Thread.sleep(20);
                    return genericCache.get(Const.MERCHANT_PREFIX+merchantId,MerchantVo.class);
                }
        }catch (Exception e){
                if(e instanceof InterruptedException){
                    Thread.currentThread().interrupt();
                }
                 log.error("query merchant information fail:{} ", e.getMessage());
                throw e;
        }finally {
                if(lock != null && lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
    }

}
