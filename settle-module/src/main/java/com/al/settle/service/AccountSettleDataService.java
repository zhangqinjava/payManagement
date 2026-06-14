package com.al.settle.service;

import com.al.settle.dto.AccountSettleSnapshot;

import java.time.LocalDate;

public interface AccountSettleDataService {
    AccountSettleSnapshot fetchSettleData(String merchantNo, LocalDate startDate, LocalDate endDate) throws Exception;
}
