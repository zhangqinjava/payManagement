package com.al.service;

import com.al.bean.dto.MerchantSettleBindDTO;
import com.al.bean.vo.MerchantSettleConfigVo;

public interface MerchantSettleConfigService {
    String bindAccount(MerchantSettleBindDTO merchantSettleBindDTO);
    MerchantSettleConfigVo queryByMerchant(String merchantNo, String busiType);
    String discard(String merchantNo, String busiType);
}
