package com.ai.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_set_merchant")
@Builder
public class MerchantVo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 商户ID
     */
    private String merchantId;
    /**
     * 商户名称
     */
    private String merchantName;
    /**
     * 商户类型
     */
    private String merchantType;
    /**
     * 状态 1-正常 2-冻结 3-注销
     */
    private Integer status;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactPhone;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
}
