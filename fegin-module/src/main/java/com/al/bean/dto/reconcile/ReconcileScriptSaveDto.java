package com.al.bean.dto.reconcile;

import lombok.Data;

@Data
public class ReconcileScriptSaveDto {
    private Long id;
    private String scriptCode;
    private String scriptName;
    private String scriptType;
    private String channelCode;
    private String scriptContent;
    private Integer status;
    private String remark;
    private String createUser;
}
