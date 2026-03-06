package com.al.bean.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_channel_ex")
@Builder
public class OrderChannelVo {
  private String  channelOrderNo;
  private String  tradeNo        ;
  private String  channelCode    ;
  private Integer  channelStatus  ;
  private String  channelResp    ;
  private String  createTime     ;
  private String  updateTime     ;
}
