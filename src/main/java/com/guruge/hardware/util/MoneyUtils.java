package com.guruge.hardware.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtils() {
    }

    public static BigDecimal round(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return value.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return round(safe(a).add(safe(b)));
    }

    public static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return round(safe(a).subtract(safe(b)));
    }

    public static BigDecimal mul(BigDecimal a, BigDecimal b) {
        return round(safe(a).multiply(safe(b)));
    }

    public static BigDecimal mul(BigDecimal a, int qty) {
        return round(safe(a).multiply(BigDecimal.valueOf(qty)));
    }

    public static BigDecimal div(BigDecimal a, BigDecimal b) {
        if (b == null || BigDecimal.ZERO.compareTo(b) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return safe(a).divide(safe(b), SCALE, ROUNDING);
    }

    /** Returns amount * percent / 100. */
    public static BigDecimal percentage(BigDecimal amount, BigDecimal percent) {
        if (amount == null || percent == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return round(amount.multiply(percent).divide(BigDecimal.valueOf(100), SCALE + 4, ROUNDING));
    }

    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
