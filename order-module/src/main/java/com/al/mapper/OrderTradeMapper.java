package com.al.mapper;

import com.al.bean.vo.OrderTradeVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderTradeMapper extends BaseMapper<OrderTradeVo> {

    int updateStatus(@Param("tradeNo") String tradeNo,
                     @Param("oldStatus") Integer oldStatus,
                     @Param("newStatus") Integer newStatus,
                     @Param("failReason") String failReason);
}
