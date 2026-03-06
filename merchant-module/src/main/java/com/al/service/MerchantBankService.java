package com.al.service;

import com.al.bean.dto.MerchantBankDto;
import com.al.bean.vo.MerchantBankVo;
import java.util.List;

public interface MerchantBankService {
    List<MerchantBankVo> query(MerchantBankDto merchantBankDto);
    MerchantBankVo update(MerchantBankDto merchantBankDto);
    MerchantBankVo save(MerchantBankDto merchantBankDto);
    String delete(MerchantBankDto merchantBankDto);
    MerchantBankVo queryById(Long id) throws Exception;
}
