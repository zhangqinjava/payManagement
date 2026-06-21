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
@TableName("reconcile_task")
public class ReconcileTaskVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private String reconcileDate;
    private String channelCode;
    private String parseScriptCode;
    private String compareScriptCode;
    private String merchantNo;
    private Integer status;
    private Integer localCount;
    private Integer remoteCount;
    private Integer diffCount;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
