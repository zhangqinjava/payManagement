package com.al.common.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FeeTypeEnum {
    INTERNAL_BUCKLE("0","现金账户扣款"),
    HANDLING_FEE("1","手续费账户扣款"),
    OFFLINE_PAYMENT("2","线下缴费");
    private String code;
    private String desc;
    public static FeeTypeEnum getByCode(String code) {
        for (FeeTypeEnum feeTypeEnum : FeeTypeEnum.values()) {
            if (feeTypeEnum.getCode().equals(code)) {
                return feeTypeEnum;
            }
        }
        return null;
    }
    public static Boolean exist(String code) {
        return getByCode(code) != null;
    }
}
