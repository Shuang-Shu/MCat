package com.mdc.mcat.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgUtils {
    public static Map<String, List<String>> parseArgs(String[] args) {
        Map<String, List<String>> result = new HashMap<>();
        int p = 0;
        while (p < args.length) {
            while (p < args.length && !args[p].startsWith("--")) {
                p++;
            }
            if (p >= args.length) {
                break;
            }
            String key = args[p];
            p++;
            List<String> values = new ArrayList<>();
            while (p < args.length && !args[p].startsWith("--")) {
                values.add(args[p]);
                p++;
            }
            result.put(key, values);
        }
        return result;
    }
}
