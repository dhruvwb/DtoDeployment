package com.ng.dtogen.util;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class XmlDtoGenerator {

    private static String PREFIX = "";
    private static boolean INCLUDE_ANNOTATIONS = true;

    public String generateDtoFromXml(String xml, String rootClassName, String prefix, boolean includeAnnotations) throws Exception {

        PREFIX = prefix == null ? "" : prefix.trim();
        INCLUDE_ANNOTATIONS = includeAnnotations;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();
        String rootTag = stripNamespace(root.getTagName());  // ⭐ FIX 1

        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();

        sb.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        sb.append("import lombok.Data;\n");

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

        String innerRootClass = PREFIX + toCamel(rootTag);

        writeField(sb, rootTag, innerRootClass, false, "    ");

        writeClassHeader(sb, innerRootClass, "    ");
        generateFields(sb, root, generated, "        ");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    private void generateFields(StringBuilder sb, Element element, Set<String> generated, String indent) {

        NodeList children = element.getChildNodes();
        Map<String, List<Element>> grouped = new LinkedHashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element e) {
                String cleanTag = stripNamespace(e.getTagName());   // ⭐ FIX 2
                grouped.computeIfAbsent(cleanTag, k -> new ArrayList<>()).add(e);
            }
        }

        for (var entry : grouped.entrySet()) {

            String tag = entry.getKey();
            List<Element> nodes = entry.getValue();

            boolean isList = nodes.size() > 1;
            Element first = nodes.get(0);

            boolean isLeaf = isLeafElement(first);

            if (isLeaf) {
                writeField(sb, tag, "String", isList, indent);
                continue;
            }

            String className = PREFIX + toCamel(tag);

            if (isList)
                writeField(sb, tag, "List<" + className + ">", true, indent);
            else
                writeField(sb, tag, className, false, indent);

            if (generated.add(className)) {

                writeClassHeader(sb, className, indent);
                generateFields(sb, first, generated, indent + "    ");
                sb.append(indent).append("}\n\n");
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

    private void writeField(StringBuilder sb, String tag, String type, boolean isList, String indent) {

        String cleanTag = stripNamespace(tag);   // ⭐ FIX 3

        if (!isCamelCase(cleanTag)) {
            sb.append(indent).append("@JsonProperty(\"").append(cleanTag).append("\")\n");
        }

        String fieldName = decap(toCamel(cleanTag));
        sb.append(indent).append("private ").append(type).append(" ").append(fieldName).append(";\n\n");
    }

    // REMOVE namespace prefix: "emp:Skills" → "Skills"
    private String stripNamespace(String tag) {
        if (tag.contains(":"))
            return tag.substring(tag.indexOf(":") + 1);
        return tag;
    }

    // Utility methods
    private boolean isLeafElement(Element e) {
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            if (children.item(i) instanceof Element) return false;
        return true;
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
