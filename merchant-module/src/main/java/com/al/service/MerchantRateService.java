package com.al.service;

import com.al.bean.dto.CaculateDto;
import com.al.bean.dto.MerchantFeeDto;
import com.al.bean.vo.MerchantFeeVo;

import java.util.List;

public interface MerchantRateService {
    List<MerchantFeeVo> query(MerchantFeeDto merchantFeeDto)throws Exception;
    MerchantFeeVo save(MerchantFeeDto merchantFeeDto) throws Exception;
    String update(MerchantFeeDto merchantFeeDto) throws Exception;
    String delete(String merchantId) throws Exception;
    public MerchantFeeVo selectOne(CaculateDto caculateDto) throws Exception;


}
