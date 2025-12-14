package com.ng.dtogen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonDtoGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    // To avoid duplicate class names for different structures
    private final Map<String, String> STRUCTURE_TO_CLASS = new HashMap<>();
    private final Map<String, Integer> CLASS_NAME_COUNT = new HashMap<>();

    public String generateDtoFromJson(String json, String rootPrefix, String rootClass) throws Exception {

        STRUCTURE_TO_CLASS.clear();
        CLASS_NAME_COUNT.clear();

        JsonNode rootNode = mapper.readTree(json);

        StringBuilder sb = new StringBuilder();

        sb.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n")
          .append("import com.fasterxml.jackson.annotation.JsonInclude;\n")
          .append("import com.fasterxml.jackson.annotation.JsonProperty;\n")
          .append("import lombok.Data;\n")
          .append("import java.util.List;\n\n");

        // Root class name
        String rootClassName = rootPrefix + rootClass;

        sb.append("@Data\n")
          .append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
          .append("@JsonIgnoreProperties(ignoreUnknown = true)\n")
          .append("public class ").append(rootClassName).append(" {\n\n");

        generateFields(sb, rootNode, rootPrefix, "    ");

        sb.append("}\n");

        return sb.toString();
    }

    private void generateFields(StringBuilder sb, JsonNode node, String prefix, String indent) {
        if (!node.isObject()) return;

        // Collect all fields from this object
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            String field = it.next();
            JsonNode child = node.get(field);

            if (child.isValueNode()) {
                // Primitive
                sb.append(indent).append("@JsonProperty(\"").append(field).append("\")\n");
                sb.append(indent).append("private String ").append(decap(field)).append(";\n\n");
            } else if (child.isObject()) {
                // Object → merge structure
                String className = resolveClassName(prefix, field, child);

                sb.append(indent).append("@JsonProperty(\"").append(field).append("\")\n");
                sb.append(indent).append("private ").append(className).append(" ")
                  .append(decap(field)).append(";\n\n");

                sb.append(indent).append("@Data\n")
                  .append(indent).append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
                  .append(indent).append("@JsonIgnoreProperties(ignoreUnknown = true)\n")
                  .append(indent).append("public static class ").append(className).append(" {\n\n");

                generateFields(sb, child, prefix, indent + "    ");

                sb.append(indent).append("}\n\n");
            } else if (child.isArray()) {
                if (child.size() == 0) {
                    // Empty array → default to list of strings
                    sb.append(indent).append("@JsonProperty(\"").append(field).append("\")\n");
                    sb.append(indent).append("private List<String> ").append(decap(field)).append(";\n\n");
                } else if (child.get(0).isValueNode()) {
                    // Primitive list
                    sb.append(indent).append("@JsonProperty(\"").append(field).append("\")\n");
                    sb.append(indent).append("private List<String> ").append(decap(field)).append(";\n\n");
                } else if (child.get(0).isObject()) {
                    // Merge all objects in array to avoid missing fields
                    JsonNode merged = mergeArrayObjects(child);
                    String className = resolveClassName(prefix, field, merged);

                    sb.append(indent).append("@JsonProperty(\"").append(field).append("\")\n");
                    sb.append(indent).append("private List<").append(className).append("> ")
                      .append(decap(field)).append(";\n\n");

                    sb.append(indent).append("@Data\n")
                      .append(indent).append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
                      .append(indent).append("@JsonIgnoreProperties(ignoreUnknown = true)\n")
                      .append(indent).append("public static class ").append(className).append(" {\n\n");

                    generateFields(sb, merged, prefix, indent + "    ");

                    sb.append(indent).append("}\n\n");
                }
            }
        }
    }

    // Merge all objects in an array into one combined object (union of fields)
    private JsonNode mergeArrayObjects(JsonNode array) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, JsonNode> mergedFields = new LinkedHashMap<>();

        for (JsonNode obj : array) {
            if (obj.isObject()) {
                obj.fieldNames().forEachRemaining(f -> {
                    JsonNode existing = mergedFields.get(f);
                    JsonNode current = obj.get(f);
                    if (existing == null) {
                        mergedFields.put(f, current);
                    } else if (existing.isObject() && current.isObject()) {
                        // merge nested objects
                        mergedFields.put(f, mergeObjects(existing, current));
                    }
                });
            }
        }
        return mapper.valueToTree(mergedFields);
    }

    // Merge two objects (union of fields)
    private JsonNode mergeObjects(JsonNode a, JsonNode b) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, JsonNode> mergedFields = new LinkedHashMap<>();

        a.fieldNames().forEachRemaining(f -> mergedFields.put(f, a.get(f)));
        b.fieldNames().forEachRemaining(f -> {
            if (!mergedFields.containsKey(f)) {
                mergedFields.put(f, b.get(f));
            } else {
                JsonNode existing = mergedFields.get(f);
                JsonNode current = b.get(f);
                if (existing.isObject() && current.isObject()) {
                    mergedFields.put(f, mergeObjects(existing, current));
                }
            }
        });
        return mapper.valueToTree(mergedFields);
    }

    private String resolveClassName(String prefix, String field, JsonNode structure) {
        String signature = computeSignature(structure);

        if (STRUCTURE_TO_CLASS.containsKey(signature)) {
            return STRUCTURE_TO_CLASS.get(signature);
        }

        String baseName = prefix + cap(field);

        int count = CLASS_NAME_COUNT.getOrDefault(baseName, 0);
        String finalName = (count == 0) ? baseName : baseName + count;
        CLASS_NAME_COUNT.put(baseName, count + 1);

        STRUCTURE_TO_CLASS.put(signature, finalName);

        return finalName;
    }

    private String computeSignature(JsonNode node) {
        if (!node.isObject()) return "";

        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            JsonNode child = node.get(key);
            if (child.isObject()) {
                sb.append(key).append("{").append(computeSignature(child)).append("}");
            } else if (child.isArray()) {
                sb.append(key).append("[array]");
            } else {
                sb.append(key).append("[value]");
            }
            sb.append("|");
        }
        return sb.toString();
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String decap(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
