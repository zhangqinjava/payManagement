package com.al.reconcile.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScriptTypeEnum {
    PARSE("PARSE", "解析脚本"),
    COMPARE("COMPARE", "比对脚本"),
    ALL("ALL", "解析+比对");

    private final String code;
    private final String desc;
}
