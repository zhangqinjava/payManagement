package com.al.auth.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermissionVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permCode;
    private String permName;
    private String permType;
    private Long parentId;
    private String path;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
