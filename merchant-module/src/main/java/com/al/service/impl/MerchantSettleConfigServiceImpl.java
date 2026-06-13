package com.al.service.impl;

import com.al.bean.dto.MerchantSettleBindDTO;
import com.al.bean.vo.MerchantSettleConfigVo;
import com.al.common.business.Const;
import com.al.common.exception.BusinessException;
import com.al.config.GenericCache;
import com.al.mapper.MerchantSettleConfigMapper;
import com.al.service.MerchantSettleConfigService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class MerchantSettleConfigServiceImpl extends ServiceImpl<MerchantSettleConfigMapper,MerchantSettleConfigVo> implements MerchantSettleConfigService {
    @Autowired
    private MerchantSettleConfigMapper merchantSettleConfigMapper;
    @Autowired
    private GenericCache genericCache;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String bindAccount(MerchantSettleBindDTO dto) {
        try {
            MerchantSettleConfigVo config = merchantSettleConfigMapper.selectOne(
                    Wrappers.lambdaQuery(MerchantSettleConfigVo.class)
                            .eq(MerchantSettleConfigVo::getMerchantNo, dto.getMerchantNo())
                            .eq(MerchantSettleConfigVo::getBusiType, dto.getBusiType())
            );
            if (config != null) {
                throw new BusinessException("结算账户配置已存在");
            }
            MerchantSettleConfigVo entity = new MerchantSettleConfigVo();
            BeanUtils.copyProperties(dto, entity);
            entity.setStatus(1);
            this.save(entity);
            return "结算配置添加成功";
        }catch (Exception e){
            log.error("settle config insert error:{}",e.getMessage());
            throw e;
        }
    }

    @Override
    public MerchantSettleConfigVo queryByMerchant(String merchantNo, String busiType) {
        String cacheKey = Const.SETTLE_CACHE_PREFIX + merchantNo + busiType;
        try {
            MerchantSettleConfigVo cached = genericCache.get(cacheKey, MerchantSettleConfigVo.class);
            if (cached != null) {
                return cached;
            }
            if (genericCache.isNullCached(cacheKey)) {
                return null;
            }
            RLock lock = redissonClient.getLock(Const.SETTLE_LOCK + merchantNo + busiType);
            try {
                if (!lock.tryLock()) {
                    return genericCache.get(cacheKey, MerchantSettleConfigVo.class);
                }
                cached = genericCache.get(cacheKey, MerchantSettleConfigVo.class);
                if (cached != null) {
                    return cached;
                }
                MerchantSettleConfigVo config = merchantSettleConfigMapper.selectOne(Wrappers.<MerchantSettleConfigVo>lambdaQuery()
                        .eq(MerchantSettleConfigVo::getMerchantNo, merchantNo)
                        .eq(MerchantSettleConfigVo::getBusiType, busiType)
                );
                if (config == null) {
                    genericCache.cacheNull(cacheKey);
                    return null;
                }
                genericCache.set(cacheKey, config);
                return config;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("queryByMerchant error:{}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String discard(String merchantNo, String busiType) {
        try {
            MerchantSettleConfigVo config = merchantSettleConfigMapper.selectOne(Wrappers.<MerchantSettleConfigVo>lambdaQuery()
                    .eq(MerchantSettleConfigVo::getMerchantNo, merchantNo)
                    .eq(MerchantSettleConfigVo::getBusiType, busiType)
            );
            if (config == null) {
                return "结算配置不存在";
            }
            config.setStatus(0);
            merchantSettleConfigMapper.updateById(config);
            genericCache.delete(Const.SETTLE_CACHE_PREFIX + merchantNo + busiType);
            return "结算配置取消成功";
        }catch (Exception e){
            log.error("discard error:{}",e.getMessage());
            throw e;
        }
    }
}
