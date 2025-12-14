package com.ng.dtogen.detect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng.dtogen.model.SupportedFormat;
import com.ng.dtogen.util.CaseUtil;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonFormatDetector implements FormatDetector {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean canHandle(String payload) {
        try {
            mapper.readTree(payload);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SupportedFormat getFormat() {
        return SupportedFormat.JSON;
    }

    @Override
    public Map<String, String> extractFieldMap(String payload) {
        try {
            JsonNode root = mapper.readTree(payload);
            Map<String, String> flat = new LinkedHashMap<>();
            flatten(root, "", flat);
            return flat;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON", e);
        }
    }

    private void flatten(JsonNode node, String path, Map<String, String> out) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> f = fields.next();
                flatten(f.getValue(), join(path, f.getKey()), out);
            }
        } else if (node.isArray()) {
            out.put(path, CaseUtil.toCamel(getLastSegment(path)) + "List");
        } else {
            String javaField = CaseUtil.toCamel(getLastSegment(path));
            out.put(path, javaField);
        }
    }

    private String join(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }

    private String getLastSegment(String path) {
        int idx = path.lastIndexOf('.');
        return idx == -1 ? path : path.substring(idx + 1);
    }
}
