package com.al.service.impl;

import com.al.bean.business.ChannelConfigStatusEnum;
import com.al.bean.dto.MerchantChannelConfigDto;
import com.al.bean.vo.MerchantChannelConfigVo;
import com.al.bean.vo.MerchantVo;
import com.al.common.exception.BusinessException;
import com.al.mapper.MerchantChannelConfigMapper;
import com.al.service.MerchantChannelConfigService;
import com.al.service.MerchantService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class MerchantChannelConfigServiceImpl implements MerchantChannelConfigService {
    @Autowired
    private MerchantChannelConfigMapper merchantChannelConfigMapper;
    @Autowired
    private MerchantService merchantService;
    @Override
    public List<MerchantChannelConfigVo> list(MerchantChannelConfigDto dto) throws Exception{
        try {
            log.info("merchantChannelConfigDto query  request param:{}", dto);
            List<MerchantChannelConfigVo> merchantChannelConfigVos = merchantChannelConfigMapper.selectList(Wrappers.lambdaQuery(MerchantChannelConfigVo.class)
                    .eq(dto.getChannelCode() != null, MerchantChannelConfigVo::getChannelCode, dto.getChannelCode())
                    .eq(dto.getMerchantNo() != null, MerchantChannelConfigVo::getMerchantNo, dto.getMerchantNo())
                    .eq(dto.getStatus() != null, MerchantChannelConfigVo::getStatus, dto.getStatus())
                    .eq(dto.getPriority() != null, MerchantChannelConfigVo::getPriority, dto.getPriority())
            );
            return merchantChannelConfigVos;
        }catch (Exception e){
            log.error("当前查询报错:{}",e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantChannelConfigVo save(MerchantChannelConfigDto dto) throws Exception{
        try {
            log.info("merchantChannelConfigDto save request param:{}", dto);
            MerchantVo query = merchantService.query(dto.getMerchantNo());
            if(Objects.isNull(query)){
                throw new BusinessException("商户信息不存在" );
            }
            MerchantChannelConfigVo build = MerchantChannelConfigVo.builder()
                    .merchantNo(dto.getMerchantNo())
                    .channelCode(dto.getChannelCode())
                    .channelName(dto.getChannelName())
                    .maxQps(dto.getMaxQps())
                    .timeoutMs(dto.getTimeoutMs())
                    .priority(dto.getPriority())
                    .weight(dto.getWeight())
                    .status(ChannelConfigStatusEnum.NORMAL.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            merchantChannelConfigMapper.insert(build);
            return build;
        }catch (Exception e){
            log.error("插入商户通道配置报错:{}",e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(MerchantChannelConfigDto merchantChannelConfigDto) throws Exception{
        try{
            log.info("merchantChannelConfigDto update request param:{}", merchantChannelConfigDto);
            MerchantChannelConfigVo build = MerchantChannelConfigVo.builder()
                    .merchantNo(merchantChannelConfigDto.getMerchantNo())
                    .channelCode(merchantChannelConfigDto.getChannelCode())
                    .channelName(merchantChannelConfigDto.getChannelName())
                    .maxQps(merchantChannelConfigDto.getMaxQps())
                    .timeoutMs(merchantChannelConfigDto.getTimeoutMs())
                    .priority(merchantChannelConfigDto.getPriority())
                    .weight(merchantChannelConfigDto.getWeight())
                    .status(merchantChannelConfigDto.getStatus())
                    .updateTime(LocalDateTime.now())
                    .build();
            merchantChannelConfigMapper.update(build,Wrappers.lambdaUpdate(MerchantChannelConfigVo.class)
                    .eq(MerchantChannelConfigVo::getMerchantNo, build.getMerchantNo())
                    .eq(MerchantChannelConfigVo::getChannelCode, build.getChannelCode())
            );
            return "更新成功";
        }catch (Exception e){
            log.error("update merchant channel config error:{}",e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(Integer id) throws Exception{
        merchantChannelConfigMapper.deleteById(id);
        return "删除成功";
    }
}
