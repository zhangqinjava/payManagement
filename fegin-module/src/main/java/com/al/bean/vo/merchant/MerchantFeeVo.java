package com.al.bean.vo.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("merchant_fee_rate")
public class MerchantFeeVo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     *商户号
     */
    private String merchantNo ;
    /**
     * 业务类型0-付款1-退款2-收单
     */
    private Integer bizType ;
    /**
     * 计费模式(1-固定2-费率3-混合)
     */
    private Integer feeMode;
    /**
     * 费率(如0.006)
     */
    private BigDecimal rate;
    /**
     * 固定手续费(分)
     */
    private BigDecimal fixedFee;
    /**
     * 最低手续费
     */
    private BigDecimal minFee ;
    /**
     * 最高手续费
     */
    private BigDecimal maxFee;
    /**
     * 币种
     */
    private String currency;
    /**
     * 状态 0-停用 1-生效
     */
    private Integer  status;
    /**
     * 生效时间
     */
    private String effectiveTime;
    /**
     * 失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 创建时间
     */
    private String createTime ;
    /**
     * 修改时间
     */
    private String updateTime;
    /**
     * 创建人姓名
     */
    private String createUser;
    /**
     * 更新人姓名
     */
    private String updateUser;

}
