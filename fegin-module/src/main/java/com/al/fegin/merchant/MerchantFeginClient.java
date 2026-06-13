package com.al.fegin.merchant;

import com.al.bean.dto.account.MerchantChannelConfigDto;
import com.al.bean.dto.merchant.*;
import com.al.bean.vo.merchant.*;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "merchant-module",path = "/merchant")
public interface MerchantFeginClient {
    @PostMapping("/info/save")
    Result<MerchantVo> save(@RequestBody MerchantDto merchantDto) throws Exception;

    @PostMapping("/info/saveWithAccount")
    Result<MerchantOnboardVo> saveWithAccount(@RequestBody MerchantOnboardDto dto) throws Exception;

    @PostMapping("/info/update")
    Result<MerchantVo> update(@RequestBody MerchantDto merchantDto) throws Exception;
    @GetMapping("/info/query")
    public Result<MerchantVo>query(@RequestParam("merchantNo")  String merchantNo) throws Exception;
    @GetMapping("/info/delete")
    public Result<MerchantVo>delete(@RequestParam("merchantNo")  String merchantNo) throws Exception;
    @PostMapping("/caculate/fee")
    public Result<CaculateVo>caculate(@RequestBody CaculateDto caculateDto) throws Exception;
    @GetMapping("/fee/query")
    Result<List<MerchantFeeVo>> queryFee(@RequestParam("merchantFeeDto") MerchantFeeDto merchantFeeDto) throws Exception;

    @PostMapping("/fee/query")
    Result<List<MerchantFeeVo>> queryFeeByBody(@RequestBody MerchantFeeDto merchantFeeDto) throws Exception;

    @PostMapping("/fee/save")
    Result<MerchantFeeVo> saveFee(@RequestBody MerchantFeeDto merchantFeeDto) throws Exception;

    @PostMapping("/fee/update")
    Result<String> updateFee(@RequestBody MerchantFeeDto merchantFeeDto) throws Exception;

    @PostMapping("/fee/delete")
    Result<String> deleteFee(@RequestBody MerchantFeeDto merchantFeeDto) throws Exception;
    @PostMapping("/bank/query")
    public Result<MerchantBankVo> queryBank(@RequestBody MerchantBankDto merchantBankDto) throws Exception;
    @GetMapping("/account/listByMerchant")
    Result<List<MerchantAccountBindVo>> listByMerchant(@RequestParam("merchantNo") String merchantNo,
                                                       @RequestParam(value = "acctType", required = false) String acctType) throws Exception;
    @PostMapping("/channel/list")
    public Result<List<MerchantChannelConfigVo>> listConfig( @RequestBody  MerchantChannelConfigDto merchantChannelConfigDto) throws Exception;
}
