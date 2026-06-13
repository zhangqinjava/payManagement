package com.al.bean.dto.account;

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
