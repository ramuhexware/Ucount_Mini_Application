package com.freddieapp.underwriting.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinancialMathUtil {

    public static BigDecimal roundTwoDecimals(BigDecimal val) {
        return val != null ? val.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
