package com.al.bean.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "channel_config")
public class ChannelConfigVo {
    private Integer id;
    private String channelCode;
    private String channelName;
    private String merchantNo;
    private Integer weight;
    private Integer priority;
    private String status;
    private Integer maxQps;
    private Integer timeoutMs;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
