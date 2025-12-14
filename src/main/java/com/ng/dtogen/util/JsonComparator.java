package com.ng.dtogen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.Iterator;
import java.util.Map;

public class JsonComparator {

    public static String compare(String baseJson, String compareJson) {
        StringBuilder result = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode baseNode = mapper.readTree(baseJson);
            JsonNode compareNode = mapper.readTree(compareJson);
            compareNodes("", baseNode, compareNode, result);
        } catch (Exception e) {
            return "❌ JSON parsing error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "✅ No differences found.";
    }

    private static void compareNodes(String path, JsonNode baseNode, JsonNode compareNode, StringBuilder result) {
        if (baseNode.getNodeType() != compareNode.getNodeType()) {
            result.append("Mismatch at ").append(path).append(": ")
                  .append(baseNode.getNodeType()).append(" vs ").append(compareNode.getNodeType()).append("\n");
            return;
        }

        if (baseNode.getNodeType() == JsonNodeType.OBJECT) {
            Iterator<Map.Entry<String, JsonNode>> fields = baseNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode compareValue = compareNode.get(fieldName);
                if (compareValue == null) {
                    result.append("Field missing in compare: ").append(path).append(".").append(fieldName).append("\n");
                } else {
                    compareNodes(path + "." + fieldName, entry.getValue(), compareValue, result);
                }
            }
        } else if (baseNode.getNodeType() == JsonNodeType.ARRAY) {
            int minLength = Math.min(baseNode.size(), compareNode.size());
            for (int i = 0; i < minLength; i++) {
                compareNodes(path + "[" + i + "]", baseNode.get(i), compareNode.get(i), result);
            }
            if (baseNode.size() != compareNode.size()) {
                result.append("Array size mismatch at ").append(path)
                      .append(": ").append(baseNode.size())
                      .append(" vs ").append(compareNode.size()).append("\n");
            }
        } else if (!baseNode.equals(compareNode)) {
            result.append("Value mismatch at ").append(path)
                  .append(": ").append(baseNode).append(" vs ").append(compareNode).append("\n");
        }
    }
}
