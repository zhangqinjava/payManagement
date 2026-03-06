package com.al.service;

import com.al.bean.vo.MerchantTerminalBindVo;

import java.util.List;

public interface MerchantTerminalBindService {

    // 绑定终端
    void bindTerminal(MerchantTerminalBindVo bind) throws Exception;

    // 解绑终端
    void unbindTerminal(String merchantNo, String terminalNo, String updateUser);

    // 查询商户绑定终端列表
    List<MerchantTerminalBindVo> getTerminalsByMerchant(String merchantNo);

    // 查询终端绑定商户
    MerchantTerminalBindVo getMerchantByTerminal(String terminalNo);
}
