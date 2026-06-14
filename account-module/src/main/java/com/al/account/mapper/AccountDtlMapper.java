package com.al.account.mapper;


import com.al.account.bean.dto.AccountQueryDto;
import com.al.account.bean.dto.QuerySummaryDto;
import com.al.account.bean.vo.AccountDtlVo;
import com.al.account.bean.vo.AccountQueryDtlVo;
import com.al.account.bean.vo.AccountSummaryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountDtlMapper extends BaseMapper<AccountDtlVo> {
    int batchInsert(List<AccountDtlVo> list);
    List<AccountQueryDtlVo> queryDetailList(@Param(value = "dto") AccountQueryDto accountQueryDto);
    AccountSummaryVo querySummary(@Param(value = "dto") QuerySummaryDto dto);
}
