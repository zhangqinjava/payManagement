package com.al.account.bean.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchUpDto {
    @NotEmpty(message = "批量上账列表不能为空")
    @Valid
    private List<AccountUpDownDto> items;
}
