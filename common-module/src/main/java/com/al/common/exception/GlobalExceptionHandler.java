package com.al.common.exception;

import com.al.common.result.Result;
import com.al.common.result.ResultEnum;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleException(BusinessException e) {
        if(StringUtils.isEmpty(e.getCode())){
            return Result.error(ResultEnum.ERROR.getCode(),e.getMsg(),null);
        }else{
            return Result.error(e.getCode(),e.getMsg(),null);
        }
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.error(msg,null);
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(e.getMessage(),null);
    }

}
