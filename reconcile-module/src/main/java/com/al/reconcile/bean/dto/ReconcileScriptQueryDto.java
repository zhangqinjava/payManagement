package com.al.reconcile.bean.dto;

import lombok.Data;

@Data
public class ReconcileScriptQueryDto {
    private String scriptCode;
    private String scriptType;
    private String channelCode;
    private Integer status;
}
