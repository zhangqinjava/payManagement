package com.al.common.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountTradeEnum {
    INIT(0,"init"),
    SUCESS(1,"sucess"),
    FAIL(2,"fial");
    private Integer code;
    private String msg;

}
