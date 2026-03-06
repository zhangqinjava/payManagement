package com.al.service;

import com.al.bean.dto.CaculateDto;
import com.al.bean.vo.CaculateVo;

public interface CaculateService {



    CaculateVo caculate(CaculateDto caculateDto) throws Exception;

}
