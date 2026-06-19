package com.al.bean.vo.reconcile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileDiffVo {
    private Long id;
    private String taskNo;
    private String diffType;
    private String bizKey;
    private BigDecimal localAmount;
    private BigDecimal remoteAmount;
    private BigDecimal diffAmount;
    private String diffDetail;
    private Integer status;
    private LocalDateTime createTime;
}
