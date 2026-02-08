package com.al.account.controller;


import com.al.common.exception.BusinessException;
import com.al.common.result.Result;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/operation")
public class accountController {
//    public Result<T> get
//    }
    public Result get(){
        return Result.success(null);
    }

}
