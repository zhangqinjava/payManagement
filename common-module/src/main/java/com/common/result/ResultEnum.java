package com.common.result;

public enum ResultEnum {
    SUCESS(200, "success"),
    ERROR(500, "error");
    private int code;
    private String msg;
    private ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
