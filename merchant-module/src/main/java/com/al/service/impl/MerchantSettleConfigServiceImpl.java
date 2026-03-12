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

@Service
@Slf4j
public class MerchantSettleConfigServiceImpl extends ServiceImpl<MerchantSettleConfigMapper,MerchantSettleConfigVo> implements MerchantSettleConfigService {
    @Autowired
    private MerchantSettleConfigMapper merchantSettleConfigMapper;
    @Autowired
    private GenericCache genericCache;
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
        try {
            MerchantSettleConfigVo merchantSettleConfigVo = genericCache.get(Const.SETTLE_CACHE_PREFIX + merchantNo + busiType, MerchantSettleConfigVo.class);
            if (merchantSettleConfigVo != null) {
                return merchantSettleConfigVo;
            }
            if (genericCache.isNullCached(Const.SETTLE_NULL_KEY + merchantNo + busiType)) {
                return null;
            }
            RLock lock = redissonClient.getLock(Const.SETTLE_LOCK + merchantNo + busiType);
            try{
                lock.lock();
                MerchantSettleConfigVo config = merchantSettleConfigMapper.selectOne(Wrappers.<MerchantSettleConfigVo>lambdaQuery()
                        .eq(MerchantSettleConfigVo::getMerchantNo, merchantNo)
                        .eq(MerchantSettleConfigVo::getBusiType, busiType)
                );
                if (config == null) {
                    genericCache.set(Const.SETTLE_NULL_KEY+merchantNo+busiType, config);
                    return null;
                }
                genericCache.set(Const.SETTLE_CACHE_PREFIX+merchantNo+busiType, config);
                return config;

            }catch (Exception e){
                log.error("query database error:{}",e.getMessage());
                throw e;
            }finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }catch (Exception e){
            log.error("queryByMerchant error:{}",e.getMessage());
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
