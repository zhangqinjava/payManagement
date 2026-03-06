package com.al.service;


import com.al.bean.vo.MerchantAccountBindVo;

import java.util.List;

public interface MerchantAccountBindService {

    // 绑定账户
    void bindAccount(MerchantAccountBindVo bind) throws Exception;

    // 解绑账户
    void unbindAccount(String merchantNo, String accountNo, String updateUser);

    // 查询商户绑定账户列表
    List<MerchantAccountBindVo> getAccountsByMerchant(String merchantNo);

    // 查询账户绑定商户
    List<MerchantAccountBindVo> getMerchantsByAccount(String accountNo);
}
