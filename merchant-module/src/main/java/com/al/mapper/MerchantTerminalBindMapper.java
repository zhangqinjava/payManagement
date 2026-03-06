package com.al.mapper;

import com.al.bean.vo.MerchantTerminalBindVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantTerminalBindMapper extends BaseMapper<MerchantTerminalBindVo> {
    // 额外可加自定义 SQL 方法
}
