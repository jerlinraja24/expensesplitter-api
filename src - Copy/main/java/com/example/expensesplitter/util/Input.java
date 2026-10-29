package com.example.expensesplitter.util;

import com.example.expensesplitter.exception.ApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Input {

    private Input() {}

    public static String text(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    public static String raw(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    /** Returns null when the value is empty. */
    public static LocalDate date(Map<String, Object> body, String key) {
        String s = text(body, key);
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "Enter dates in the format yyyy-MM-dd.");
        }
    }

    /** Returns defaultValue when empty, otherwise a value rounded to 2 decimals. */
    public static BigDecimal money(Map<String, Object> body, String key, BigDecimal defaultValue) {
        String s = text(body, key);
        if (s.isEmpty()) {
            return defaultValue;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Enter a valid amount.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new ApiException(400, "Use at most 2 decimal places.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static List<Long> ids(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        List<Long> result = new ArrayList<>();
        if (v instanceof List<?> list) {
            for (Object o : list) {
                try {
                    Long id = Long.valueOf(String.valueOf(o));
                    if (!result.contains(id)) {
                        result.add(id);
                    }
                } catch (NumberFormatException e) {
                    throw new ApiException(400, "Invalid person selected.");
                }
            }
        }
        return result;
    }
}
