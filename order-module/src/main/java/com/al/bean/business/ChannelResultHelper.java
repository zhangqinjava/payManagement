package com.al.bean.business;

import java.util.Map;

public final class ChannelResultHelper {

    private ChannelResultHelper() {
    }

    public static boolean isPaySuccess(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        if (result instanceof Map) {
            Object code = ((Map<?, ?>) result).get("code");
            if (code != null) {
                return "SUCCESS".equalsIgnoreCase(String.valueOf(code))
                        || "200".equals(String.valueOf(code));
            }
            Object success = ((Map<?, ?>) result).get("success");
            if (success instanceof Boolean) {
                return (Boolean) success;
            }
        }
        String text = result.toString();
        return "success".equalsIgnoreCase(text) || "SUCCESS".equalsIgnoreCase(text);
    }

    public static boolean isRefundSuccess(Object result) {
        return isPaySuccess(result);
    }
}
