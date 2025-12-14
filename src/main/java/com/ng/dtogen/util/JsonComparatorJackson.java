package com.ng.dtogen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class JsonComparatorJackson {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Main comparison method
     */
    public String compareJson(JsonNode currentJson, JsonNode expectedJson) {

        // 1. STRUCTURE SCHEMA for both JSONs
        String currentSchema = buildSchema(currentJson, "");
        String expectedSchema = buildSchema(expectedJson, "");

        // 2. Key extraction for missing/extra detection
        Set<String> currentKeys = new LinkedHashSet<>();
        Set<String> expectedKeys = new LinkedHashSet<>();

        extractPaths(currentJson, "", currentKeys);
        extractPaths(expectedJson, "", expectedKeys);

        Set<String> missing = new LinkedHashSet<>(expectedKeys);
        missing.removeAll(currentKeys);

        Set<String> extra = new LinkedHashSet<>(currentKeys);
        extra.removeAll(expectedKeys);

        // 3. Final Output
        StringBuilder result = new StringBuilder();

        result.append("============= CURRENT JSON STRUCTURE =============\n");
        result.append(currentSchema).append("\n\n");

        result.append("============= EXPECTED JSON STRUCTURE ============\n");
        result.append(expectedSchema).append("\n\n");

        result.append("============= MISSING FIELDS (Expected > Current) =============\n");
        missing.forEach(field -> result.append(field).append("\n"));

        result.append("\n============= EXTRA FIELDS (Current > Expected) =============\n");
        extra.forEach(field -> result.append(field).append("\n"));

        return result.toString();
    }

    /**
     * Build Tree-like schema representation
     */
    private String buildSchema(JsonNode node, String indent) {
        StringBuilder sb = new StringBuilder();

        if (node.isObject()) {
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                sb.append(indent).append(field).append("\n");
                sb.append(buildSchema(node.get(field), indent + "    "));
            }
        } else if (node.isArray()) {
            sb.append(indent).append("[]\n");
            for (int i = 0; i < node.size(); i++) {
                sb.append(indent).append("  [").append(i).append("]\n");
                sb.append(buildSchema(node.get(i), indent + "        "));
            }
        }

        return sb.toString();
    }

    /**
     * Key path extraction for missing/extra comparison
     */
    private void extractPaths(JsonNode node, String currentPath, Set<String> keys) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                String newPath = currentPath.isEmpty() ? field : currentPath + "." + field;
                extractPaths(node.get(field), newPath, keys);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = currentPath + "[" + i + "]";
                extractPaths(node.get(i), newPath, keys);
            }
        } else {
            keys.add(currentPath);
        }
    }
}
