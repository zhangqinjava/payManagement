package com.ai.service.impl;

import com.ai.bean.dto.MerchantBankDto;
import com.ai.bean.vo.MerchantBankVo;
import com.ai.config.GenericCache;
import com.ai.mapper.MerchantBankMapper;
import com.ai.service.MerchantBankService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.business.EncrypUtil;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MerchantBankServiceImpl implements MerchantBankService {
    @Autowired
    private MerchantBankMapper merchantBankMapper;
    @Autowired
    private GenericCache genericCache;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public List<MerchantBankVo> query(MerchantBankDto merchantBankDto) {
        try {
            if (StringUtil.isBlank(merchantBankDto.getMerchantId())) {
                throw new BusinessException("商户号为必输选项");
            }
            List<MerchantBankVo> bankList = merchantBankMapper.selectList(Wrappers.lambdaQuery(MerchantBankVo.class)
                    .eq(MerchantBankVo::getMerchantId, merchantBankDto.getMerchantId())
                    .eq(merchantBankDto.getId() != null,MerchantBankVo::getId, merchantBankDto.getId())
                    .eq(merchantBankDto.getIsDefault() != null, MerchantBankVo::getIsDefault, merchantBankDto.getIsDefault())
                    .eq(merchantBankDto.getCardType() != null, MerchantBankVo::getCardType, merchantBankDto.getCardType())
                    .eq(merchantBankDto.getBankCode() != null, MerchantBankVo::getBankCode, merchantBankDto.getBankCode())
                    .eq(merchantBankDto.getCardName() != null, MerchantBankVo::getCardName, merchantBankDto.getCardName())
                    .eq(merchantBankDto.getCardNo() != null,MerchantBankVo::getCardNoEncrypt , EncrypUtil.encrypt(merchantBankDto.getCardNo())));
            return bankList;
        }catch (Exception e){
            log.error("merchantBank information query error", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantBankVo update(MerchantBankDto merchantBankDto) {
        try {
            MerchantBankVo build = MerchantBankVo.builder()
                    .merchantId(merchantBankDto.getMerchantId())
                    .bankCode(merchantBankDto.getBankCode())
                    .bankName(merchantBankDto.getBankName())
                    .bindStatus(Integer.valueOf(merchantBankDto.getStatus()))
                    .cardName(merchantBankDto.getCardName())
                    .cardNoEncrypt(EncrypUtil.encrypt(merchantBankDto.getCardNo()))
                    .cardNoMask(Const.ENCRYPT_PREFIX)
                    .cardType(merchantBankDto.getCardType())
                    .isDefault(merchantBankDto.getIsDefault())
                    .bindTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyymmdd hh:mm:ss")))
                    .remark(merchantBankDto.getRemark())
                    .updatedTime(DateFormat.getDateTimeInstance().format(new Date())).build();
            int num = merchantBankMapper.update(build, Wrappers.lambdaUpdate(MerchantBankVo.class)
                    .eq(MerchantBankVo::getMerchantId, merchantBankDto.getMerchantId())
                    .eq(MerchantBankVo::getId, merchantBankDto.getId()));
            return num > 0 ? build : null;
        }catch (Exception e){
            log.error("merchantBank update information error", e);
            throw e;

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantBankVo save(MerchantBankDto merchantBankDto) {
        try{
            //四要素鉴权需要自及去接入来校验 持卡人姓名 持卡人身份证 持卡人卡号 持卡人手机号
            //调用银行卡信息获取，卡类型、联行号、
            MerchantBankVo build = MerchantBankVo.builder()
                    .merchantId(merchantBankDto.getMerchantId())
                    .bankCode(merchantBankDto.getBankCode())
                    .bankName(merchantBankDto.getBankName())
                    .bindStatus(Integer.valueOf(BusiEnum.NORMAL.getCode()))
                    .cardName(merchantBankDto.getCardName())
                    .cardNoEncrypt(EncrypUtil.encrypt(merchantBankDto.getCardNo()))
                    .cardNoMask(Const.ENCRYPT_PREFIX)
                    .cardType(merchantBankDto.getCardType())
                    .isDefault(merchantBankDto.getIsDefault())
                    .bindTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .remark(merchantBankDto.getRemark())
                    .updatedTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .createdTime(DateFormat.getDateTimeInstance().format(new Date())).build();
            int insert = merchantBankMapper.insert(build);
            if(insert <=0){
                throw new BusinessException("新增商户结算卡信息失败");
            }
            return build;
        }catch (Exception e){
            log.error("merchantBank information error", e);
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException("结算卡信息已经存在");
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(MerchantBankDto merchantBankDto) {
        try {
            int delete = merchantBankMapper.delete(Wrappers.lambdaUpdate(MerchantBankVo.class)
                    .eq(merchantBankDto.getId() != null, MerchantBankVo::getId, merchantBankDto.getId())
                    .eq(merchantBankDto.getMerchantId() != null, MerchantBankVo::getMerchantId, merchantBankDto.getBankCode())
                    .eq(merchantBankDto.getCardNo() != null, MerchantBankVo::getIdCardEncrypt, EncrypUtil.encrypt(merchantBankDto.getCardNo())));
            if (delete > 0) {
                return "删除成功";
            }else{
                return "删除失败";
            }
        }catch (Exception e){
            log.error("merchantBank delete information error", e);
            throw e;
        }
    }
    public MerchantBankVo queryById(Long id) throws Exception {
        RLock lock =null;
        try {
            MerchantBankVo merchantBankVo = genericCache.get(Const.BANK_PREFIX + id, MerchantBankVo.class);
            if (merchantBankVo != null) {
                return merchantBankVo;
            }
            //判断有没有空值缓存
            if (genericCache.isNullCached(Const.BANK_NULL_PREFIX + id)) {
                return null;
            }
            //加分布式锁
            lock= redissonClient.getLock(Const.BANK_LOCK + id);
            if (lock.tryLock(200,TimeUnit.MILLISECONDS)) {
                //读数据库
                MerchantBankVo merchantBankVo1 = merchantBankMapper.selectById(id);
                if (merchantBankVo1 == null) {
                    //数据库不存在，缓存空值，防止击穿
                    genericCache.cacheNull(Const.BANK_NULL_PREFIX ,String.valueOf(id));
                    return null;
                }
                //缓存数据库
                genericCache.set(Const.BANK_PREFIX + id, merchantBankVo1);
                return merchantBankVo1;
            }else{
                MerchantBankVo merchantBankVo1 = genericCache.get(Const.BANK_PREFIX + id, MerchantBankVo.class);
                return  merchantBankVo1;
            }
        }catch (Exception e){
            log.error("merchantBank queryById information error", e);
            throw e;
        }finally {
            if (lock !=null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
