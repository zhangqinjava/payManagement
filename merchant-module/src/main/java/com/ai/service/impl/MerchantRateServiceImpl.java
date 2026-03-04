package com.ai.service.impl;

import com.ai.bean.dto.CaculateDto;
import com.ai.bean.dto.MerchantFeeDto;
import com.ai.bean.vo.MerchantFeeVo;
import com.ai.config.GenericCache;
import com.ai.mapper.MerchantFeeMapper;
import com.ai.service.MerchantRateService;
import com.al.common.business.BusiEnum;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
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

    @Override
    public List<MerchantFeeVo> query(MerchantFeeDto merchantFeeDto) throws Exception {
        try {
            log.info("query all merchant rate start:{} ", merchantFeeDto);
            if (StringUtil.isBlank(merchantFeeDto.getMerchantNo())) {
                throw new RuntimeException("商户号不能为空!");
            }
            List<MerchantFeeVo> merchantFeeVos = merchantFeeMapper.selectList(Wrappers.lambdaQuery(MerchantFeeVo.class)
                    .eq(merchantFeeDto.getId() != null, MerchantFeeVo::getId, merchantFeeDto.getId())
                    .eq(merchantFeeDto.getFeeMode() != null, MerchantFeeVo::getFeeMode, merchantFeeDto.getFeeMode())
                    .eq(merchantFeeDto.getBizType() != null, MerchantFeeVo::getBizType, merchantFeeDto.getBizType())
                    .eq(merchantFeeDto.getMerchantNo() != null, MerchantFeeVo::getMerchantNo, merchantFeeDto.getMerchantNo())
                    .eq(merchantFeeDto.getStatus() != null, MerchantFeeVo::getStatus, merchantFeeDto.getStatus())
                    .eq(merchantFeeDto.getEffectiveTime() != null, MerchantFeeVo::getEffectiveTime, merchantFeeDto.getEffectiveTime())
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
            MerchantFeeVo build = MerchantFeeVo.builder()
                    .bizType(Integer.valueOf(merchantFeeDto.getBizType()))
                    .rate(merchantFeeDto.getRate())
                    .fixedFee(merchantFeeDto.getFixedFee())
                    .feeMode(Integer.valueOf(merchantFeeDto.getFeeMode()))
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
            throw e;
        }
    }

    @Override
    public String update(MerchantFeeDto merchantFeeDto) throws Exception {
        return "";
    }

    @Override
    public String delete(String merchantId) throws Exception {
        return "";
    }

    @Override
    public MerchantFeeVo selectOne(CaculateDto dto) throws Exception {
        try {
            log.info("query one merchant rate start:{} ", dto);
            MerchantFeeVo result =
                    merchantFeeMapper.selectOne(
                            Wrappers.<MerchantFeeVo>lambdaQuery()
                                    .eq(MerchantFeeVo::getMerchantNo, dto.getMerchantId())
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
