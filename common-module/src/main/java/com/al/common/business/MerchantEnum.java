package com.al.common.business;

public enum MerchantEnum {
    NOMAL(1,"正常","商户状态正常"),
    FREEZE(2,"冻结","商户状态冻结"),
    CANCEL(3,"注销","商户状态注销");

    private Integer code;
    private String msg;
    private String desc;
    MerchantEnum(Integer code, String msg, String desc) {
        this.code = code;
        this.msg = msg;
        this.desc = desc;
    }
    public Integer getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public String getDesc() {
        return desc;
    }
    public static MerchantEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MerchantEnum merchantEnum : values()) {
            if (merchantEnum.getCode().equals(code)) {
                return merchantEnum;
            }
        }
        return null;
    }
}
