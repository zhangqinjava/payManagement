package com.ai.service;

import com.ai.bean.dto.MerchantBankDto;
import com.ai.bean.vo.MerchantBankVo;
import java.util.List;

public interface MerchantBankService {
    List<MerchantBankVo> query(MerchantBankDto merchantBankDto);
    MerchantBankVo update(MerchantBankDto merchantBankDto);
    MerchantBankVo save(MerchantBankDto merchantBankDto);
    String delete(MerchantBankDto merchantBankDto);
    MerchantBankVo queryById(Long id) throws Exception;
}
