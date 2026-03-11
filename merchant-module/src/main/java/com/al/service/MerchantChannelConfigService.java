package com.al.service;

import com.al.bean.dto.MerchantChannelConfigDto;
import com.al.bean.vo.MerchantChannelConfigVo;

import java.util.List;

public interface MerchantChannelConfigService {
    List<MerchantChannelConfigVo> list(MerchantChannelConfigDto merchantChannelConfigDto) throws Exception;
    MerchantChannelConfigVo save(MerchantChannelConfigDto merchantChannelConfigDto) throws Exception;
    String update(MerchantChannelConfigDto merchantChannelConfigDto) throws Exception;
    String delete(Integer id) throws Exception;
}
