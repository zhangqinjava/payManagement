package com.al.split.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SplitReceiverTypeEnum {
    PLATFORM_FEE("FEE", "平台手续费"),
    MERCHANT_SETTLE("NET", "商户待清分"),
    PARTNER("PARTNER", "合作方分账");

    private final String code;
    private final String msg;
}
