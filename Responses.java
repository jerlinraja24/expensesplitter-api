package com.example.expensesplitter.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Responses {

    private Responses() {}

    /** A success response: {"success": true, ...key/value pairs}. */
    public static Map<String, Object> ok(Object... pairs) {
        Map<String, Object> m = map(pairs);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.putAll(m);
        return result;
    }

    /** A plain map built from key/value pairs. Null values are allowed. */
    public static Map<String, Object> map(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            m.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return m;
    }
}
