package com.al.account.mapper;

import com.al.account.bean.dto.AccountDto;
import com.al.account.bean.vo.AccountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper extends BaseMapper<AccountVo> {
    List<AccountVo> selectForUpdate(List<String> list);

}
