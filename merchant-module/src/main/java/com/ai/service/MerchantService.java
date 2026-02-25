package com.ai.service;

import com.ai.bean.dto.MerchantDto;
import com.ai.bean.vo.MerchantVo;

import java.util.List;

public interface MerchantService {
    MerchantVo save(MerchantDto merchantDto) throws Exception;
    MerchantVo update(MerchantDto merchantDto) throws Exception;
    String delete(String  merchantId) throws Exception;
    MerchantVo query(String merchantId) throws Exception;

}
