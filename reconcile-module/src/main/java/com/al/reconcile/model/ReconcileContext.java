package com.al.reconcile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileContext {
    private String reconcileDate;
    private String channelCode;
    private String merchantNo;
    private String taskNo;
    @Builder.Default
    private Map<String, Object> ext = new HashMap<>();
}
