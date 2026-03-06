package com.al.bean.business;

import com.al.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeeModeEnum {

    RATE(1, "按比例"),
    FIXED(2, "固定金额"),
    MIXED(3, "混合费率");

    private final int code;
    private final String desc;

    public static FeeModeEnum of(Integer code) {
        for (FeeModeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new BusinessException("不支持的费率模式：" + code);
    }
}
