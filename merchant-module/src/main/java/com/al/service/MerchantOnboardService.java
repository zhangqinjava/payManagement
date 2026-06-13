package com.al.service;

import com.al.bean.dto.MerchantOnboardDto;
import com.al.bean.vo.MerchantOnboardVo;

public interface MerchantOnboardService {
    MerchantOnboardVo onboard(MerchantOnboardDto dto) throws Exception;
}
