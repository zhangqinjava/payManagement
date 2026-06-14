package com.al.bean.vo.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchUpResultVo {
    private int successCount;
    private int failCount;
    private List<AccountUpDownVo> successList;
    private List<String> failMessages;
}
