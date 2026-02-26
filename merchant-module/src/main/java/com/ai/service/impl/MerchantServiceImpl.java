package com.ai.service.impl;

import com.ai.bean.dto.MerchantDto;
import com.ai.bean.vo.MerchantVo;
import com.ai.config.MerchantCache;
import com.ai.mapper.MerchantMapper;
import com.ai.service.MerchantService;
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
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Resource
    private MerchantCache merchantCache;
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private MerchantMapper merchantMapper;

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
                            merchantCache.delete(merchantDto.getMerchantId());

                            // 延迟双删
                            CompletableFuture.runAsync(() -> {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {}
                                merchantCache.delete(merchantDto.getMerchantId());
                            });
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
                            merchantCache.delete(merchantId);

                            // 延迟双删
                            CompletableFuture.runAsync(() -> {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {}
                                merchantCache.delete(merchantId);
                            });
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
            MerchantVo merchant = merchantCache.get(merchantId);
            if (merchant != null) {
                return merchant;
            }
            // 2. 空值缓存判断
            if (merchantCache.isNullCached(merchantId)) {
                return null;
            }
            String lockKey = "m:lock:" + merchantId;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                // 3. 防击穿加锁
                if (lock.tryLock(50, 5, TimeUnit.SECONDS)) {
                    // double check
                    merchant = merchantCache.get(merchantId);
                    if (merchant != null) {
                        return merchant;
                    }
                    // 4. DB 回源
                     merchant = merchantMapper.selectOne(Wrappers.lambdaQuery(MerchantVo.class).eq(MerchantVo::getMerchantId, merchantId));
                    if (merchant == null) {
                        merchantCache.cacheNull(merchantId);
                        return null;
                    }
                    // 5. 写缓存
                    merchantCache.set(merchant);
                    return merchant;
                }else {
                    // 没拿到锁，短暂 sleep 后再读缓存
                    Thread.sleep(20);
                    return merchantCache.get(merchantId);

                }
        }catch (Exception e){
                if(e instanceof InterruptedException){
                    Thread.currentThread().interrupt();
                }
                 log.error("query merchant information fail:{} ", e.getMessage());
                throw new BusinessException("查询失败");
        }finally {
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
    }

}
