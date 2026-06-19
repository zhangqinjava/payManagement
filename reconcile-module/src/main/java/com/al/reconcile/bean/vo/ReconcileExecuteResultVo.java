package com.al.reconcile.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileExecuteResultVo {
    private String taskNo;
    private String reconcileDate;
    private String channelCode;
    private Integer status;
    private Integer localCount;
    private Integer remoteCount;
    private Integer diffCount;
    private List<ReconcileDiffVo> diffs;
}
