package com.ai.service;

import com.ai.bean.dto.CaculateDto;
import com.ai.bean.dto.MerchantFeeDto;
import com.ai.bean.vo.MerchantFeeVo;
import com.ai.bean.vo.MerchantVo;

import java.util.List;

public interface MerchantRateService {
    List<MerchantFeeVo> query(MerchantFeeDto merchantFeeDto)throws Exception;
    MerchantFeeVo save(MerchantFeeDto merchantFeeDto) throws Exception;
    String update(MerchantFeeDto merchantFeeDto) throws Exception;
    String delete(String merchantId) throws Exception;
    public MerchantFeeVo selectOne(CaculateDto caculateDto) throws Exception;


}
