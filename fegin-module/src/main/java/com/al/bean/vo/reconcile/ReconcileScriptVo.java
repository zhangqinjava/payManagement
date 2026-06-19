package com.al.bean.vo.reconcile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileScriptVo {
    private Long id;
    private String scriptCode;
    private String scriptName;
    private String scriptType;
    private String channelCode;
    private String scriptContent;
    private Integer version;
    private Integer status;
    private String remark;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
