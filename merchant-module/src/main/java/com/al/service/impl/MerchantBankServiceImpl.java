package com.al.service.impl;

import com.al.bean.dto.MerchantBankDto;
import com.al.bean.vo.MerchantBankVo;
import com.al.bean.vo.MerchantVo;
import com.al.config.GenericCache;
import com.al.mapper.MerchantBankMapper;
import com.al.service.MerchantBankService;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.util.EncrypUtil;
import com.al.common.exception.BusinessException;
import com.al.service.MerchantService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private MerchantService merchantService;
    @Autowired
    @Qualifier("customRestTemplate")
    private RestTemplate restTemplate;
    @Override
    public List<MerchantBankVo> query(MerchantBankDto merchantBankDto) throws Exception {
        try {
            log.info("merchant query infomation param:{}", merchantBankDto);
            if (StringUtil.isBlank(merchantBankDto.getMerchantNo())) {
                throw new BusinessException("商户号为必输选项");
            }
            List<MerchantBankVo> bankList = merchantBankMapper.selectList(Wrappers.lambdaQuery(MerchantBankVo.class)
                    .eq(MerchantBankVo::getMerchantNo, merchantBankDto.getMerchantNo())
                    .eq(merchantBankDto.getId() != null,MerchantBankVo::getId, merchantBankDto.getId())
                    .eq(merchantBankDto.getIsDefault() != null, MerchantBankVo::getIsDefault, merchantBankDto.getIsDefault())
                    .eq(merchantBankDto.getCardType() != null, MerchantBankVo::getCardType, merchantBankDto.getCardType())
                    .eq(merchantBankDto.getBankCode() != null, MerchantBankVo::getBankCode, merchantBankDto.getBankCode())
                    .eq(merchantBankDto.getCardName() != null, MerchantBankVo::getCardName, merchantBankDto.getCardName()));
            return bankList;
        }catch (Exception e){
            log.error("merchantBank information query error", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantBankVo update(MerchantBankDto merchantBankDto) throws Exception {
        try {
            MerchantBankVo build = MerchantBankVo.builder()
                    .merchantNo(merchantBankDto.getMerchantNo())
                    .bankCode(merchantBankDto.getBankCode())
                    .bankName(merchantBankDto.getBankName())
                    .bindStatus(Integer.valueOf(merchantBankDto.getStatus()))
                    .cardName(merchantBankDto.getCardName())
                    .cardNoEncrypt(EncrypUtil.encrypt(merchantBankDto.getCardNo()))
                    .cardNoMask(Const.ENCRYPT_PREFIX)
                    .cardType(Integer.valueOf(merchantBankDto.getCardType()))
                    .isDefault(merchantBankDto.getIsDefault())
                    .bindTime(LocalDateTime.now())
                    .remark(merchantBankDto.getRemark())
                    .updatedTime(LocalDateTime.now()).build();
            int num = merchantBankMapper.update(build, Wrappers.lambdaUpdate(MerchantBankVo.class)
                    .eq(MerchantBankVo::getMerchantNo, merchantBankDto.getMerchantNo())
                    .eq(MerchantBankVo::getId, merchantBankDto.getId()));
            return num > 0 ? build : null;
        }catch (Exception e){
            log.error("merchantBank update information error", e);
            throw e;

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantBankVo save(MerchantBankDto merchantBankDto) throws Exception{
        try{
            log.info("merchant bank infomation save param:{}",merchantBankDto);
            MerchantVo result = merchantService.query(merchantBankDto.getMerchantNo());
            if(Objects.isNull(result)){
                log.info("merchant infomation not exist");
                throw new BusinessException("商户信息不存在");
            }
            //四要素鉴权需要自及去接入来校验 持卡人姓名 持卡人身份证 持卡人卡号 持卡人手机号
            //调用四要素接口
//            Object o = RestTemplateUtil.postJson(restTemplate, "", Object.class, Object.class);
            //调用银行卡信息获取，卡类型、联行号、
            MerchantBankVo build = MerchantBankVo.builder()
                    .merchantNo(merchantBankDto.getMerchantNo())
                    .bankCode(merchantBankDto.getBankCode())
                    .bankName(merchantBankDto.getBankName())
                    .bindStatus(Integer.valueOf(BusiEnum.NORMAL.getCode()))
                    .cardName(merchantBankDto.getCardName())
                    .cardNoEncrypt(EncrypUtil.encrypt(merchantBankDto.getCardNo()))
                    .cardNoMask(Const.ENCRYPT_PREFIX)
                    .cardType(Integer.valueOf(merchantBankDto.getCardType()))
                    .isDefault(merchantBankDto.getIsDefault())
                    .bindTime(LocalDateTime.now())
                    .remark(merchantBankDto.getRemark())
                    .updatedTime(LocalDateTime.now())
                    .createdTime(LocalDateTime.now()).build();
             merchantBankMapper.insert(build);
            return build;
        }catch (Exception e){
            log.error("merchantBank information error:{}", e);
            if (e instanceof DuplicateKeyException) {
                throw new BusinessException("商户结算卡信息已经存在");
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(MerchantBankDto merchantBankDto) throws Exception {
        try {
            int delete = merchantBankMapper.delete(Wrappers.lambdaUpdate(MerchantBankVo.class)
                    .eq(merchantBankDto.getId() != null, MerchantBankVo::getId, merchantBankDto.getId())
                    .eq(merchantBankDto.getMerchantNo() != null, MerchantBankVo::getMerchantNo, merchantBankDto.getBankCode())
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
