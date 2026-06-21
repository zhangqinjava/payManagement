package com.al.reconcile.bean.dto;

import lombok.Data;

@Data
public class ReconcileTaskQueryDto {
    private String taskNo;
    private String reconcileDate;
    private String channelCode;
    private String merchantNo;
    private Integer status;
}
