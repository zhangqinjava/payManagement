package com.al.settle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算状态枚举
 */
@Getter
@AllArgsConstructor
public enum SettleStatusEnum {
    /**
     * 待结算
     */
    PENDING(0, "待结算"),
    /**
     * 结算中
     */
    PROCESSING(1, "结算中"),
    /**
     * 结算成功
     */
    SUCCESS(2, "结算成功"),
    /**
     * 结算失败
     */
    FAILED(3, "结算失败"),
    ;

    private final Integer code;
    private final String desc;

    public static SettleStatusEnum getByCode(Integer code) {
        for (SettleStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}