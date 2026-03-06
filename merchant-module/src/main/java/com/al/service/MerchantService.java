package com.al.service;

import com.al.bean.dto.MerchantDto;
import com.al.bean.vo.MerchantVo;

public interface MerchantService {
    MerchantVo save(MerchantDto merchantDto) throws Exception;
    MerchantVo update(MerchantDto merchantDto) throws Exception;
    String delete(String  merchantId) throws Exception;
    MerchantVo query(String merchantId) throws Exception;

}
