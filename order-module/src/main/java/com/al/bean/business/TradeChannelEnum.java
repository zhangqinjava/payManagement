package com.al.bean.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TradeChannelEnum {
    WX_PAY(1,"微信"),
    ALI_PAY(2,"支付宝");
    private Integer code;
    private String msg;
    public  static TradeChannelEnum getByCode(Integer code){
        for (TradeChannelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }

        }
        return null;
    }
}
