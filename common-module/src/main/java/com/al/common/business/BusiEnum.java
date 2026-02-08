package com.al.common.business;

public enum BusiEnum {
    IN("1","增","余额上账"),
    OUT("2","减","余额下账"),
    //币种
    RMB("rmb","人民币","人民币"),
    //状态
    NORMAL("0","正常","账户正常"),
    FREEZE("1","冻结","商户冻结"),
    CLOSE("2","注销","商户注销"),
    //账户类型
    CASH("201","现金","商户可见账户"),
    SETTLE("208","待清分","待清分账户")
    ;


    private String code;
    private String msg;
    private String desc;


    BusiEnum(String code, String msg, String desc) {
        this.code = code;
        this.msg = msg;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    public String getMsg() {
        return msg;
    }
    public static boolean contains(String code) {
        for (BusiEnum busiEnum : BusiEnum.values()) {
            if (busiEnum.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
