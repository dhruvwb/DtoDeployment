package com.ng.dtogen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonDtoGenerator {

    private static String PREFIX = "";
    private static boolean INCLUDE_ANNOTATIONS = true;

    private final ObjectMapper mapper = new ObjectMapper();

    public String generateDtoFromJson(
            String json,
            String rootClassName,
            String prefix,
            boolean includeAnnotations) throws Exception {

        PREFIX = prefix == null ? "" : prefix.trim();
        INCLUDE_ANNOTATIONS = includeAnnotations;

        JsonNode rootNode = mapper.readTree(json);

        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();

        sb.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.util.List;\n");

        if (INCLUDE_ANNOTATIONS) {
            sb.append("import com.fasterxml.jackson.annotation.JsonInclude;\n");
            sb.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n");
        }

        sb.append("\n");

        sb.append("@Data\n");
        if (INCLUDE_ANNOTATIONS) {
            sb.append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n");
            sb.append("@JsonIgnoreProperties(ignoreUnknown = true)\n");
        }
        sb.append("public class ").append(rootClassName).append(" {\n\n");

        generateFields(sb, rootNode, generated, "    ");

        sb.append("}\n");

        return sb.toString();
    }

    private void generateFields(
            StringBuilder sb,
            JsonNode node,
            Set<String> generated,
            String indent) {

        if (!node.isObject())
            return;

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode child = entry.getValue();

            if (child.isValueNode()) {
                writeField(sb, fieldName, "String", false, indent);
            }

            else if (child.isObject()) {
                String className = PREFIX + toCamel(fieldName);
                writeField(sb, fieldName, className, false, indent);

                if (generated.add(className)) {
                    writeClassHeader(sb, className, indent);
                    generateFields(sb, child, generated, indent + "    ");
                    sb.append(indent).append("}\n\n");
                }
            }

            else if (child.isArray()) {
                if (child.isEmpty() || child.get(0).isValueNode()) {
                    writeField(sb, fieldName, "List<String>", true, indent);
                } else if (child.get(0).isObject()) {
                    JsonNode merged = mergeArrayObjects(child);
                    String className = PREFIX + toCamel(fieldName);

                    writeField(sb, fieldName, "List<" + className + ">", true, indent);

                    if (generated.add(className)) {
                        writeClassHeader(sb, className, indent);
                        generateFields(sb, merged, generated, indent + "    ");
                        sb.append(indent).append("}\n\n");
                    }
                }
            }
        }
    }

    private void writeClassHeader(StringBuilder sb, String className, String indent) {
        sb.append(indent).append("@Data\n");
        if (INCLUDE_ANNOTATIONS) {
            sb.append(indent).append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n");
            sb.append(indent).append("@JsonIgnoreProperties(ignoreUnknown = true)\n");
        }
        sb.append(indent).append("public static class ").append(className).append(" {\n\n");
    }

    private void writeField(
            StringBuilder sb,
            String jsonField,
            String type,
            boolean isList,
            String indent) {

        if (!isCamelCase(jsonField)) {
            sb.append(indent)
                    .append("@JsonProperty(\"")
                    .append(jsonField)
                    .append("\")\n");
        }

        String fieldName = decap(toCamel(jsonField));
        sb.append(indent)
                .append("private ")
                .append(type)
                .append(" ")
                .append(fieldName)
                .append(";\n\n");
    }

    // ----------- Helpers -------------

    private JsonNode mergeArrayObjects(JsonNode array) {
        Map<String, JsonNode> merged = new LinkedHashMap<>();

        for (JsonNode obj : array) {
            if (obj.isObject()) {
                obj.fields().forEachRemaining(e -> {
                    merged.putIfAbsent(e.getKey(), e.getValue());
                });
            }
        }
        return mapper.valueToTree(merged);
    }

    private boolean isCamelCase(String s) {
        return s.matches("^[a-z]+([A-Z][a-z0-9]+)*$");
    }

    private String toCamel(String s) {
        s = s.replace("-", " ").replace("_", " ");
        StringBuilder out = new StringBuilder();
        for (String part : s.split(" ")) {
            if (!part.isBlank()) {
                out.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return out.toString();
    }

    private String decap(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
