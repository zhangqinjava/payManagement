package com.al.settle.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReconcileJobParam {
    private String reconcileDate;
    private String channelCode;
    private String parseScriptCode;
    private String compareScriptCode;
    private List<ReconcileMerchantItem> items;
}
