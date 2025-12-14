package com.ng.dtogen.util;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SoapDtoGenerator {

    private static String PREFIX = "";
    private static boolean INCLUDE_ANNOTATIONS = true;

    public String generateDtoFromSoap(
            String xml,
            String rootClassName,
            String prefix,
            boolean includeAnnotations
    ) throws Exception {

        PREFIX = prefix == null ? "" : prefix.trim();
        INCLUDE_ANNOTATIONS = includeAnnotations;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();
        String rootTag = stripNamespace(root.getTagName());
        String envelopeClass = PREFIX + toClassName(rootTag);

        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();

        // imports
        sb.append("import jakarta.xml.bind.annotation.XmlAccessType;\n")
          .append("import jakarta.xml.bind.annotation.XmlAccessorType;\n")
          .append("import jakarta.xml.bind.annotation.XmlElement;\n")
          .append("import jakarta.xml.bind.annotation.XmlRootElement;\n")
          .append("import java.util.List;\n\n");

        // root wrapper
        if (INCLUDE_ANNOTATIONS) {
            sb.append("@XmlRootElement(name = \"").append(rootTag).append("\")\n");
        }
        sb.append("@XmlAccessorType(XmlAccessType.FIELD)\n");
        sb.append("public class ").append(rootClassName).append(" {\n\n");

        if (INCLUDE_ANNOTATIONS) {
            sb.append("    @XmlElement(name = \"").append(rootTag).append("\")\n");
        }
        sb.append("    private ").append(envelopeClass)
          .append(" ").append(toFieldName(rootTag)).append(";\n\n");

        // envelope class
        writeClassHeader(sb, envelopeClass, "    ");
        generateFields(sb, root, generated, "        ");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    // --------------------------------------------------

    private void generateFields(
            StringBuilder sb,
            Element element,
            Set<String> generated,
            String indent
    ) {

        NodeList children = element.getChildNodes();
        Map<String, List<Element>> grouped = new LinkedHashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element e) {
                String tag = stripNamespace(e.getTagName());
                grouped.computeIfAbsent(tag, k -> new ArrayList<>()).add(e);
            }
        }

        List<String> nestedClasses = new ArrayList<>();

        for (var entry : grouped.entrySet()) {

            String tag = entry.getKey();
            List<Element> elements = entry.getValue();
            Element first = elements.get(0);

            boolean isList = elements.size() > 1;
            boolean isLeaf = isLeaf(first);
            boolean isEmptyObject = !isLeaf && !hasText(first);

            String fieldName = toFieldName(tag);
            String className = PREFIX + toClassName(tag);

            // ---------------- LEAF ----------------
            if (isLeaf) {
                if (INCLUDE_ANNOTATIONS) {
                    sb.append(indent)
                      .append("@XmlElement(name = \"").append(tag).append("\")\n");
                }
                sb.append(indent)
                  .append("private ")
                  .append(isList ? "List<String>" : "String")
                  .append(" ")
                  .append(fieldName)
                  .append(";\n\n");
                continue;
            }

            // ---------------- OBJECT ----------------
            if (INCLUDE_ANNOTATIONS) {
                sb.append(indent)
                  .append("@XmlElement(name = \"").append(tag).append("\")\n");
            }

            sb.append(indent)
              .append("private ")
              .append(isList ? "List<" + className + ">" : className)
              .append(" ")
              .append(fieldName)
              .append(";\n\n");

            if (generated.add(className)) {
                StringBuilder nested = new StringBuilder();
                writeClassHeader(nested, className, indent);

                if (!isEmptyObject) {
                    generateFields(nested, first, generated, indent + "    ");
                }

                nested.append(indent).append("}\n\n");
                nestedClasses.add(nested.toString());
            }
        }

        // append nested classes AFTER fields
        nestedClasses.forEach(sb::append);
    }

    private void writeClassHeader(StringBuilder sb, String className, String indent) {
        sb.append(indent)
          .append("@XmlAccessorType(XmlAccessType.FIELD)\n");
        sb.append(indent)
          .append("public static class ").append(className).append(" {\n\n");
    }

    // --------------------------------------------------

    private boolean isLeaf(Element e) {
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) return false;
        }
        return true;
    }

    private boolean hasText(Element e) {
        return e.getTextContent() != null && !e.getTextContent().trim().isEmpty();
    }

    private String stripNamespace(String tag) {
        return tag.contains(":") ? tag.substring(tag.indexOf(":") + 1) : tag;
    }

    // XML tag → Java ClassName
    private String toClassName(String s) {
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

    // XML tag → Java fieldName
    private String toFieldName(String s) {
        String className = toClassName(s);
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
