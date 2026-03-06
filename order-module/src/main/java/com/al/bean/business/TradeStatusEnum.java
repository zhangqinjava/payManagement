package com.al.bean.business;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TradeStatusEnum {
    INIT(0,"init"),
    PAYING(1,"支付中"),
    SUCCESS(2,"订单成功"),
    FAIL(3,"订单失败"),
    CLOSED(4,"订单关闭");

    private  int code;
    private String msg;
}
