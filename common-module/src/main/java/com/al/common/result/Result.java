package com.al.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultEnum.SUCESS.getCode(),ResultEnum.SUCESS.getMsg(),data);
    }
    public static <T> Result<T> success(String msg,T data) {
        return new Result<>(ResultEnum.SUCESS.getCode(),msg,data);
    }
    public static <T> Result<T> success(int code,String msg,T data) {
        return new Result<>(code,msg,data);
    }
    public static <T> Result<T> success(ResultEnum resultEnum,T data) {
        return new Result<>(resultEnum.getCode(),resultEnum.getMsg(),data);
    }

    public static <T> Result<T> error(T data) {
        return new Result<>(ResultEnum.ERROR.getCode(),ResultEnum.ERROR.getMsg(),data);
    }
    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(ResultEnum.ERROR.getCode(), msg,data);
    }
    public static <T> Result<T> error(int code,String msg, T data) {
        return new Result<>(code, msg,data);
    }

    public static <T> Result<T> error(ResultEnum resultEnum, T data) {
        return new Result<>(resultEnum.getCode(), resultEnum.getMsg(),data);
    }

}
