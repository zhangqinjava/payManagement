package com.al.service;

import com.al.bean.dto.MerchantBankDto;
import com.al.bean.vo.MerchantBankVo;
import java.util.List;

public interface MerchantBankService {
    List<MerchantBankVo> query(MerchantBankDto merchantBankDto)throws Exception;
    MerchantBankVo update(MerchantBankDto merchantBankDto) throws Exception;
    MerchantBankVo save(MerchantBankDto merchantBankDto) throws Exception;
    String delete(MerchantBankDto merchantBankDto) throws Exception;
    MerchantBankVo queryById(Long id) throws Exception;
}
