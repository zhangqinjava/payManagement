package com.al.bean.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChannelConfigStatusEnum {
    NORMAL(0, "normal"),
    DISABLED(1, "disabled");
    private Integer code;
    private String desc;
}
