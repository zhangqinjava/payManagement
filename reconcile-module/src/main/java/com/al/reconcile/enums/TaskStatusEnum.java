package com.al.reconcile.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PROCESSING(0, "处理中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final int code;
    private final String desc;
}
