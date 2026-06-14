package com.al.service.impl;

import com.al.bean.dto.CaculateDto;
import com.al.bean.dto.MerchantFeeDto;
import com.al.bean.vo.MerchantFeeVo;
import com.al.bean.vo.MerchantVo;
import com.al.common.exception.BusinessException;
import com.al.config.GenericCache;
import com.al.mapper.MerchantFeeMapper;
import com.al.service.MerchantRateService;
import com.al.common.business.BusiEnum;
import com.al.service.MerchantService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
@Service
@Slf4j
public class MerchantRateServiceImpl implements MerchantRateService {
    @Autowired
    private MerchantFeeMapper merchantFeeMapper;
    @Autowired
    private GenericCache genericCache;
    @Autowired
    private RedissonClient redissonClient;
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private MerchantService merchantService;

    @Override
    public List<MerchantFeeVo> query(MerchantFeeDto merchantFeeDto) throws Exception {
        try {
            log.info("query all merchant rate start:{} ", merchantFeeDto);
            if (StringUtil.isBlank(merchantFeeDto.getMerchantNo())) {
                throw new BusinessException("商户号不能为空");
            }
            List<MerchantFeeVo> merchantFeeVos = merchantFeeMapper.selectList(Wrappers.lambdaQuery(MerchantFeeVo.class)
                    .eq(merchantFeeDto.getId() != null, MerchantFeeVo::getId, merchantFeeDto.getId())
                    .eq(merchantFeeDto.getFeeMode() != null, MerchantFeeVo::getFeeMode, merchantFeeDto.getFeeMode())
                    .eq(merchantFeeDto.getBizType() != null, MerchantFeeVo::getBizType, merchantFeeDto.getBizType())
                    .eq(merchantFeeDto.getMerchantNo() != null, MerchantFeeVo::getMerchantNo, merchantFeeDto.getMerchantNo())
                    .eq(merchantFeeDto.getStatus() != null, MerchantFeeVo::getStatus, merchantFeeDto.getStatus())
                    .eq(merchantFeeDto.getFeeType() != null, MerchantFeeVo::getFeeType, merchantFeeDto.getFeeType())
                    .le(merchantFeeDto.getEffectiveTime() != null, MerchantFeeVo::getEffectiveTime, merchantFeeDto.getEffectiveTime())
                    .eq(merchantFeeDto.getCurrency() != null, MerchantFeeVo::getCurrency, merchantFeeDto.getCurrency())
                    .eq(merchantFeeDto.getCreateUser() != null, MerchantFeeVo::getCreateUser, merchantFeeDto.getCreateUser())
                    .orderByDesc(MerchantFeeVo::getEffectiveTime)
            );
            return merchantFeeVos;
        }catch (Exception e) {
            log.error("query all merchant rate error:{}", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantFeeVo save(MerchantFeeDto merchantFeeDto) throws Exception {
        try {
            log.info("save merchant rate start:{} ", merchantFeeDto);
            checkParam(merchantFeeDto);
            MerchantFeeVo build = MerchantFeeVo.builder()
                    .bizType(Integer.valueOf(merchantFeeDto.getBizType()))
                    .rate(merchantFeeDto.getRate())
                    .fixedFee(merchantFeeDto.getFixedFee())
                    .feeMode(Integer.valueOf(merchantFeeDto.getFeeMode()))
                    .feeType(merchantFeeDto.getFeeType())
                    .merchantNo(merchantFeeDto.getMerchantNo())
                    .currency(merchantFeeDto.getCurrency())
                    .maxFee(merchantFeeDto.getMaxFee())
                    .minFee(merchantFeeDto.getMinFee())
                    .status(Integer.valueOf(BusiEnum.RATE_NOT_DISABLED.getCode()))
                    .effectiveTime(merchantFeeDto.getEffectiveTime())
                    .createUser(merchantFeeDto.getCreateUser())
                    .updateUser(merchantFeeDto.getCreateUser())
                    .createTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                    .build();
            int insert = merchantFeeMapper.insert(build);
            log.info("save merchant rate end restlt:{}", insert);
            return build;
        }catch (Exception e) {
            log.error("save merchant rate error:{}", e);
            if (e instanceof DuplicateKeyException){
                throw new BusinessException("商户费率模式配置重复" );
            }
            throw e;
        }
    }
    public void checkParam(MerchantFeeDto merchantFeeDto) throws Exception {
        try {
            MerchantVo resule = merchantService.query(merchantFeeDto.getMerchantNo());
            if (Objects.isNull(resule)) {
                throw new BusinessException("当前商户信息不存在");
            }
            List<MerchantFeeVo> merchantFeeVos = queryFeeMode(merchantFeeDto);
            if (!CollectionUtils.isEmpty(merchantFeeVos)) {
                log.info("merchant already config feeMode:{}", merchantFeeDto);
                if (merchantFeeDto.getEffectiveTime().compareTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))) <= 0) {
                    throw new BusinessException("商户已存在此种费率配置，生效日期必须大于今天" );
                }
            }
            if (merchantFeeDto.getEffectiveTime().compareTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))) < 0) {
                throw new BusinessException("商户配置费率生效时间不能小于当天");
            }
            return;
        }catch (Exception e) {
            log.error("check merchant fee configuration error:{} ", e);
            throw e;
        }
    }
    public List<MerchantFeeVo> queryFeeMode(MerchantFeeDto merchantFeeDto) throws ParseException {
        List<MerchantFeeVo>  feeList = merchantFeeMapper.selectList(Wrappers.<MerchantFeeVo>lambdaQuery()
                .eq(MerchantFeeVo::getMerchantNo, merchantFeeDto.getMerchantNo())
                .eq(MerchantFeeVo::getBizType, merchantFeeDto.getBizType())
                .eq(MerchantFeeVo::getFeeMode, merchantFeeDto.getFeeMode())
                .eq(MerchantFeeVo::getStatus, BusiEnum.RATE_NOT_DISABLED.getCode())
                .le(MerchantFeeVo::getEffectiveTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        return feeList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(MerchantFeeDto merchantFeeDto) throws Exception {
        try {
            MerchantVo resule = merchantService.query(merchantFeeDto.getMerchantNo());
            if(Objects.isNull(resule)){
                throw new BusinessException("当前商户信息不存在" );
            }
            MerchantFeeVo build = MerchantFeeVo.builder()
                    .feeType(merchantFeeDto.getFeeType())
                    .merchantNo(merchantFeeDto.getMerchantNo())
                    .updateUser(merchantFeeDto.getUpdateUser())
                    .status(Integer.valueOf(merchantFeeDto.getStatus()))
                    .build();
            merchantFeeMapper.update(build,Wrappers.lambdaUpdate(MerchantFeeVo.class).
                    eq(MerchantFeeVo::getMerchantNo, merchantFeeDto.getMerchantNo())
                    .eq(MerchantFeeVo::getBizType, merchantFeeDto.getBizType())
                    .eq(MerchantFeeVo::getEffectiveTime, merchantFeeDto.getEffectiveTime()));
            return "商户费率更新成功";
        }catch (Exception e) {
            log.error("update merchant rate error:{}", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(MerchantFeeDto merchantFeeDto) throws Exception {
        if (merchantFeeDto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        MerchantFeeVo exist = findFeeRecord(merchantFeeDto);
        if (exist == null) {
            throw new BusinessException("费率配置不存在");
        }
        if (Integer.valueOf(BusiEnum.RATE_DISABLED.getCode()).equals(exist.getStatus())) {
            return "费率已停用";
        }
        MerchantFeeVo update = MerchantFeeVo.builder()
                .status(Integer.valueOf(BusiEnum.RATE_DISABLED.getCode()))
                .updateUser(merchantFeeDto.getUpdateUser())
                .updateTime(DateFormat.getDateTimeInstance().format(new Date()))
                .build();
        merchantFeeMapper.update(update, Wrappers.lambdaUpdate(MerchantFeeVo.class)
                .eq(MerchantFeeVo::getId, exist.getId()));
        return "商户费率停用成功";
    }

    private MerchantFeeVo findFeeRecord(MerchantFeeDto merchantFeeDto) {
        if (merchantFeeDto.getId() != null) {
            return merchantFeeMapper.selectById(merchantFeeDto.getId());
        }
        if (StringUtil.isBlank(merchantFeeDto.getMerchantNo())
                || StringUtil.isBlank(merchantFeeDto.getBizType())
                || StringUtil.isBlank(merchantFeeDto.getEffectiveTime())) {
            throw new BusinessException("请指定费率ID，或提供商户号、业务类型、生效时间");
        }
        return merchantFeeMapper.selectOne(Wrappers.lambdaQuery(MerchantFeeVo.class)
                .eq(MerchantFeeVo::getMerchantNo, merchantFeeDto.getMerchantNo())
                .eq(MerchantFeeVo::getBizType, Integer.valueOf(merchantFeeDto.getBizType()))
                .eq(MerchantFeeVo::getEffectiveTime, merchantFeeDto.getEffectiveTime()));
    }

    @Override
    public MerchantFeeVo selectOne(CaculateDto dto) throws Exception {
        try {
            log.info("query one merchant rate start:{} ", dto);
            MerchantVo resule = merchantService.query(dto.getMerchantNo());
            if(Objects.isNull(resule)){
                throw new BusinessException("当前商户信息不存在" );
            }
            MerchantFeeVo result =
                    merchantFeeMapper.selectOne(
                            Wrappers.<MerchantFeeVo>lambdaQuery()
                                    .eq(MerchantFeeVo::getMerchantNo, dto.getMerchantNo())
                                    .eq(MerchantFeeVo::getBizType, dto.getBizType())
                                    .eq(MerchantFeeVo::getFeeMode, dto.getFeeMode())
                                    .eq(MerchantFeeVo::getStatus,
                                            Integer.valueOf(BusiEnum.RATE_NOT_DISABLED.getCode()))
                                    // 已生效
                                    .le(MerchantFeeVo::getEffectiveTime, dto.getCaculateDate())
                                    .orderByDesc(MerchantFeeVo::getEffectiveTime)
                                    .last("LIMIT 1")
                    );
            return result;
        } catch (Exception e) {
            log.error("select merchant rate error:{}", e);
            throw e;
        }
    }

}
