package com.al.account.mapper;


import com.al.account.bean.vo.AccountDtlVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountDtlMapper extends BaseMapper<AccountDtlVo> {
    int batchInsert(List<AccountDtlVo> list);
}
