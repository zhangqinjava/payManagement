package com.al.common.util;

import java.math.BigDecimal;

public class BigDecimalUtil {
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }
    public static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }
    public static BigDecimal mul(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }
    public static BigDecimal div(BigDecimal a, BigDecimal b) {
        return a.divide(b);
    }
    public static boolean comapre(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0;
    }
}
