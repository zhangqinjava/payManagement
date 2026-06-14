package com.al.account.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageVo<T> {
    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> records;
}
