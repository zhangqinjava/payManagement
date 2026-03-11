package com.al.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "channel_config")
public class MerchantChannelConfigVo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String channelCode;
    private String channelName;
    private String merchantNo;
    private Integer weight;
    private Integer priority;
    private Integer status;
    private Integer maxQps;
    private Integer timeoutMs;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
