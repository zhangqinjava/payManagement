package com.al.split.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SplitStatusEnum {
    PENDING(0, "待分账"),
    PROCESSING(1, "分账中"),
    SUCCESS(2, "分账成功"),
    PARTIAL_FAIL(3, "部分失败"),
    FAIL(4, "分账失败");

    private final Integer code;
    private final String msg;
}
