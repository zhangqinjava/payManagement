package com.ai.service;

import com.ai.bean.business.FeeModeEnum;
import com.ai.bean.dto.CaculateDto;
import com.ai.bean.vo.CaculateVo;
import com.ai.bean.vo.MerchantFeeVo;

public interface CaculateService {



    CaculateVo caculate(CaculateDto caculateDto) throws Exception;

}
