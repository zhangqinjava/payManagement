package com.al.fegin.merchant;

import com.al.bean.dto.merchant.CaculateDto;
import com.al.bean.dto.merchant.MerchantBankDto;
import com.al.bean.dto.merchant.MerchantDto;
import com.al.bean.dto.merchant.MerchantFeeDto;
import com.al.bean.vo.merchant.CaculateVo;
import com.al.bean.vo.merchant.MerchantBankVo;
import com.al.bean.vo.merchant.MerchantFeeVo;
import com.al.bean.vo.merchant.MerchantVo;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "merchant-module",path = "/merchant")
public interface MerchantFeginClient {
    @PostMapping("/info/save")
    public Result<MerchantVo> save(@RequestBody MerchantDto merchantDto) throws Exception;
    @PostMapping("/info/update")
    public Result<MerchantVo>update(@RequestBody  MerchantDto merchantDto) throws Exception;
    @GetMapping("/info/query")
    public Result<MerchantVo>query(@RequestParam("merchantNo")  String merchantNo) throws Exception;
    @GetMapping("/info/delete")
    public Result<MerchantVo>delete(@RequestParam("merchantNo")  String merchantNo) throws Exception;
    @PostMapping("/caculate/fee")
    public Result<CaculateVo>caculate(@RequestBody CaculateDto caculateDto) throws Exception;
    @GetMapping("/fee/queru")
    public Result<List<MerchantFeeVo>>queryFee(@RequestParam MerchantFeeDto merchantFeeDto) throws Exception;
    @PostMapping("/bank/query")
    public Result<MerchantBankVo> queryBank(@RequestBody MerchantBankDto merchantBankDto) throws Exception;
}
