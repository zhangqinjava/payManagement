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
    SETTLE("208","待清分","待清分账户"),
    //业务类型
    BIZ_ONLINE("0","线上","线上业务"),
    BIZ_OFFLINE("1","线下","线下业务"),
    //渠道类型
    WX("1","WX","微信渠道"),
    ALIPAY("2","ALIPAY","支付宝渠道"),
    UMAPY("3","UMPAY","联动渠道"),
    LAKALA("4","LAKALA","拉卡拉渠道"),
    //功能码
    FUNCODE_UP("0601","上账","上账"),
    FUNCODE_DOWN("0602","下账","下账"),
    FUNCODE_TRANSFER("0603","转账","转账"),
    //方向
    FUN_DIRECTION_C("C","余额增加","余额增加"),
    FUN_DIRECTION_D("D","余额减少","余额减少");


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
