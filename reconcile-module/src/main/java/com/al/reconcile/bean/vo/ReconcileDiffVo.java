package com.al.reconcile.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("reconcile_diff")
public class ReconcileDiffVo {
    @TableId(type = IdType.AUTO)
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
