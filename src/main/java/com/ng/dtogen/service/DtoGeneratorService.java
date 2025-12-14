package com.ng.dtogen.service;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class DtoGeneratorService {

    private static final Map<String, String> CLASS_NAME_CACHE = new HashMap<>();
    private static final Map<String, String> STRUCTURE_SIGNATURE_MAP = new HashMap<>();

    private static String ROOT_PREFIX = "";

    /**
     * Generates DTO code from XML string.
     *
     * @param xml        XML input as string
     * @param rootPrefix Class prefix for generated DTO
     * @return Generated DTO Java code as String
     * @throws Exception
     */
    public String generateDtoFromXml(String xml, String rootPrefix) throws Exception {

        ROOT_PREFIX = rootPrefix;
        CLASS_NAME_CACHE.clear();
        STRUCTURE_SIGNATURE_MAP.clear();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();
        String rootTag = root.getTagName();

        StringBuilder sb = new StringBuilder();
        Set<String> generatedClasses = new HashSet<>();

        sb.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n")
                .append("import com.fasterxml.jackson.annotation.JsonInclude;\n")
                .append("import com.fasterxml.jackson.annotation.JsonProperty;\n")
                .append("import lombok.Data;\n\n");

        sb.append("@Data\n")
                .append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
                .append("@JsonIgnoreProperties(ignoreUnknown = true)\n")
                .append("public class ").append(ROOT_PREFIX).append("ResponseDto {\n\n");

        // Main wrapper field
        String rootClass = ROOT_PREFIX + cap(rootTag);

        sb.append("    @JsonProperty(\"").append(rootTag).append("\")\n")
                .append("    private ").append(rootClass).append(" ").append(decap(rootClass)).append(";\n\n");

        // Root class definition
        sb.append("    @Data\n")
                .append("    @JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
                .append("    @JsonIgnoreProperties(ignoreUnknown = true)\n")
                .append("    public class ").append(rootClass).append(" {\n\n");

        generateFields(sb, root, generatedClasses, "        ");

        sb.append("    }\n\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ==========================================================================================
    // FIELD + CLASS GENERATION
    // ==========================================================================================
    private void generateFields(StringBuilder sb, Element element,
                                Set<String> generatedClasses, String indent) {

        NodeList children = element.getChildNodes();
        Map<String, List<Element>> grouped = new LinkedHashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element e) {
                grouped.computeIfAbsent(e.getTagName(), k -> new ArrayList<>()).add(e);
            }
        }

        for (var entry : grouped.entrySet()) {
            String tag = entry.getKey();
            List<Element> occurrences = entry.getValue();
            boolean isList = occurrences.size() > 1;

            Element first = occurrences.get(0);
            boolean isLeaf = isLeafElement(first);

            // Leaf tag = String
            if (isLeaf) {
                if (isList) {
                    sb.append(indent).append("@JsonProperty(\"").append(tag).append("\")\n")
                            .append(indent).append("private List<String> ").append(decap(tag)).append(";\n\n");
                } else {
                    sb.append(indent).append("@JsonProperty(\"").append(tag).append("\")\n")
                            .append(indent).append("private String ").append(decap(tag)).append(";\n\n");
                }
                continue;
            }

            // Non-leaf → wrapper class based on child structure
            String signature = getStructureSignature(first);
            String className;

            if (STRUCTURE_SIGNATURE_MAP.containsKey(signature)) {
                // Reuse existing wrapper
                className = STRUCTURE_SIGNATURE_MAP.get(signature);
            } else {
                // New wrapper → name from children tags + Wrapper
                List<String> childTags = new ArrayList<>(Arrays.asList(signature.split(",")));
                String wrapperName = childTags.isEmpty() ? cap(tag) + "Wrapper" : String.join("", childTags) + "Wrapper";
                className = wrapperName;
                STRUCTURE_SIGNATURE_MAP.put(signature, className);
            }

            // FIELD declaration
            if (isList) {
                sb.append(indent).append("@JsonProperty(\"").append(tag).append("\")\n")
                        .append(indent).append("private List<").append(className).append("> ")
                        .append(decap(tag)).append(";\n\n");
            } else {
                sb.append(indent).append("@JsonProperty(\"").append(tag).append("\")\n")
                        .append(indent).append("private ").append(className).append(" ")
                        .append(decap(tag)).append(";\n\n");
            }

            // Generate class if not already done
            if (generatedClasses.add(className)) {

                sb.append(indent).append("@Data\n")
                        .append(indent).append("@JsonInclude(JsonInclude.Include.NON_EMPTY)\n")
                        .append(indent).append("@JsonIgnoreProperties(ignoreUnknown = true)\n")
                        .append(indent).append("public class ").append(className).append(" {\n\n");

                generateFields(sb, first, generatedClasses, indent + "    ");

                sb.append(indent).append("}\n\n");
            }
        }
    }

    // ==========================================================================================
    // UTILITIES
    // ==========================================================================================
    private boolean isLeafElement(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            if (children.item(i) instanceof Element) return false;
        return true;
    }

    private String getStructureSignature(Element element) {
        NodeList children = element.getChildNodes();
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element e)
                tags.add(e.getTagName());
        }
        return String.join(",", tags);
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String decap(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
