package com.al.bean.dto.reconcile;

import lombok.Data;

@Data
public class ReconcileScriptQueryDto {
    private String scriptCode;
    private String scriptType;
    private String channelCode;
    private Integer status;
}
