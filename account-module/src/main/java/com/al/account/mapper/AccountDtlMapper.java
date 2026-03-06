package com.al.account.mapper;


import com.al.account.bean.dto.AccountQueryDto;
import com.al.account.bean.vo.AccountDtlVo;
import com.al.account.bean.vo.AccountQueryDtlVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountDtlMapper extends BaseMapper<AccountDtlVo> {
    int batchInsert(List<AccountDtlVo> list);
    AccountQueryDtlVo queryDetail(@Param(value = "dto") AccountQueryDto accountQueryDto);
}
