package com.ng.dtogen.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
public class SoapDtoGenerator {

    private static final Map<String, String> SIGNATURE_CLASS_MAP = new HashMap<>();
    private static String MAIN_CLASS_NAME = "";
    private static String ROOT_PREFIX = "";

    public String generateDtoFromSoap(String xml, String rootPrefix, String mainClassName) throws Exception {

        MAIN_CLASS_NAME = mainClassName;
        ROOT_PREFIX = rootPrefix;
        SIGNATURE_CLASS_MAP.clear();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();
        String rawTag = root.getTagName();          
        String localTag = stripPrefix(rawTag);      
        String envelopeClass = ROOT_PREFIX + cap(localTag);

        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();

        // imports
        sb.append("import jakarta.xml.bind.annotation.XmlAccessType;\n")
          .append("import jakarta.xml.bind.annotation.XmlAccessorType;\n")
          .append("import jakarta.xml.bind.annotation.XmlElement;\n")
          .append("import jakarta.xml.bind.annotation.XmlRootElement;\n\n");

        // main class
        sb.append("@XmlAccessorType(XmlAccessType.FIELD)\n");
        sb.append("public class ").append(MAIN_CLASS_NAME).append(" {\n\n");

        sb.append("    @XmlElement(name = \"").append(localTag).append("\")\n");
        sb.append("    private ").append(envelopeClass).append(" ").append(decap(localTag)).append(";\n\n");

        // main envelope class
        sb.append("    @XmlAccessorType(XmlAccessType.FIELD)\n");
        sb.append("    public static class ").append(envelopeClass).append(" {\n\n");

        generateFields(sb, root, generated);

        sb.append("    }\n\n"); // end envelope
        sb.append("}\n"); // end main class

        return sb.toString();
    }

    private void generateFields(StringBuilder sb, Element element, Set<String> generated) {
        NodeList children = element.getChildNodes();
        Map<String, List<Element>> groups = new LinkedHashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element e) {
                groups.computeIfAbsent(e.getTagName(), k -> new ArrayList<>()).add(e);
            }
        }

        // Step 1: generate field declarations first
        List<String> nestedClasses = new ArrayList<>();
        for (var entry : groups.entrySet()) {
            String rawTag = entry.getKey();
            String localTag = stripPrefix(rawTag);
            Element first = entry.getValue().get(0);

            boolean leaf = isLeaf(first);

            if (leaf) {
                sb.append("        @XmlElement(name = \"").append(localTag).append("\")\n");
                sb.append("        private String ").append(decap(localTag)).append(";\n\n");
                continue;
            }

            String signature = getSignature(first);
            String className = SIGNATURE_CLASS_MAP.computeIfAbsent(signature, k -> ROOT_PREFIX + cap(localTag));

            sb.append("        @XmlElement(name = \"").append(localTag).append("\")\n");
            sb.append("        private ").append(className).append(" ").append(decap(localTag)).append(";\n\n");

            if (!generated.contains(className)) {
                generated.add(className);

                // collect nested class definition to append later
                StringBuilder nested = new StringBuilder();
                nested.append("        @XmlAccessorType(XmlAccessType.FIELD)\n");
                nested.append("        public static class ").append(className).append(" {\n\n");
                generateFields(nested, first, generated);
                nested.append("        }\n\n");
                nestedClasses.add(nested.toString());
            }
        }

        // Step 2: append nested class definitions after fields
        for (String cls : nestedClasses) {
            sb.append(cls);
        }
    }

    private boolean isLeaf(Element e) {
        NodeList kids = e.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            if (kids.item(i) instanceof Element) return false;
        }
        return true;
    }

    private String stripPrefix(String tagName) {
        return tagName.contains(":") ? tagName.substring(tagName.indexOf(":") + 1) : tagName;
    }

    private String getSignature(Element e) {
        List<String> tags = new ArrayList<>();
        NodeList kids = e.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            if (kids.item(i) instanceof Element el)
                tags.add(stripPrefix(el.getTagName()));
        }
        return String.join("_", tags);
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String decap(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
