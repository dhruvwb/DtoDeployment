// service/JsonCompareService.java
package com.ng.dtogen.service;

import com.ng.dtogen.model.JsonCompareSummary;
import com.ng.dtogen.model.JsonCompareResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonCompareService {

    public JsonCompareResponse compare(Map<String, Object> left, Map<String, Object> right) {
        Map<String, Object> leftCopy = new LinkedHashMap<>(left);
        Map<String, Object> rightCopy = new LinkedHashMap<>(right);

        List<String> extraKeys = new ArrayList<>();
        List<String> missingKeys = new ArrayList<>();
        List<String> mismatched = new ArrayList<>();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(left.keySet());
        allKeys.addAll(right.keySet());

        for (String key : allKeys) {
            Object leftVal = left.get(key);
            Object rightVal = right.get(key);

            if (!left.containsKey(key)) {
                extraKeys.add(key);
                rightCopy.put(key, colorize(rightVal, "green"));
            } else if (!right.containsKey(key)) {
                missingKeys.add(key);
                leftCopy.put(key, colorize(leftVal, "red"));
            } else if (!Objects.equals(leftVal, rightVal)) {
                mismatched.add(key);
                leftCopy.put(key, colorize(leftVal, "orange"));
                rightCopy.put(key, colorize(rightVal, "orange"));
            }
        }

        JsonCompareSummary summary = new JsonCompareSummary(extraKeys, missingKeys, mismatched);
        return new JsonCompareResponse(leftCopy, rightCopy, summary);
    }

    private Map<String, Object> colorize(Object value, String color) {
        Map<String, Object> colored = new HashMap<>();
        colored.put("value", value);
        colored.put("color", color);
        return colored;
    }
}
