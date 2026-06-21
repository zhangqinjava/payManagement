package com.al.bean.dto.reconcile;

import lombok.Data;

@Data
public class ReconcileTaskQueryDto {
    private String taskNo;
    private String reconcileDate;
    private String channelCode;
    private Integer status;
}
