package com.ng.dtogen.util;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

public class XmlComparator {

    public static String compare(String baseXml, String compareXml) {
        StringBuilder result = new StringBuilder();
        try {
            Document baseDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(baseXml.getBytes()));
            Document compareDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(compareXml.getBytes()));

            baseDoc.getDocumentElement().normalize();
            compareDoc.getDocumentElement().normalize();

            compareElements(baseDoc.getDocumentElement(), compareDoc.getDocumentElement(), "", result);

        } catch (Exception e) {
            return "❌ XML parsing error: " + e.getMessage();
        }

        return result.length() > 0 ? result.toString() : "✅ No differences found.";
    }

    private static void compareElements(Element base, Element compare, String path, StringBuilder result) {
        String currentPath = path + "/" + base.getTagName();

        // Handle missing node
        if (!base.getTagName().equals(compare.getTagName())) {
            result.append("Element mismatch at ").append(currentPath)
                    .append(": ").append(base.getTagName())
                    .append(" vs ").append(compare.getTagName()).append(" (⚠️ Possibly missing or misnamed)\n");
            return;
        }

        // Compare attributes
        NamedNodeMap baseAttrs = base.getAttributes();
        for (int i = 0; i < baseAttrs.getLength(); i++) {
            Node attr = baseAttrs.item(i);
            String baseVal = attr.getNodeValue();
            String compareVal = compare.getAttribute(attr.getNodeName());
            if (!Objects.equals(baseVal, compareVal)) {
                result.append("Attribute mismatch at ").append(currentPath)
                        .append(" [").append(attr.getNodeName()).append("]: ")
                        .append(baseVal).append(" vs ").append(compareVal).append("\n");
            }
        }

        // Compare text content
        String baseText = getTextContent(base).trim();
        String compareText = getTextContent(compare).trim();
        if (!baseText.isEmpty() || !compareText.isEmpty()) {
            if (!Objects.equals(baseText, compareText)) {
                result.append("Text content mismatch at ").append(currentPath)
                        .append(": '").append(baseText).append("' vs '")
                        .append(compareText).append("'\n");
            }
        }

        // Children
        List<Element> baseChildren = getElementChildren(base);
        List<Element> compareChildren = getElementChildren(compare);

        if (!baseChildren.isEmpty() && !compareChildren.isEmpty()) {
            if (baseChildren.size() != compareChildren.size()) {
                result.append("Child node count mismatch at ").append(currentPath)
                        .append(": ").append(baseChildren.size())
                        .append(" vs ").append(compareChildren.size()).append("\n");
            }

            // Canonical comparison: match by element tag names
            Map<String, List<Element>> compareMap = buildElementMap(compareChildren);
            for (Element baseChild : baseChildren) {
                String tag = baseChild.getTagName();
                List<Element> candidates = compareMap.getOrDefault(tag, new ArrayList<>());
                if (!candidates.isEmpty()) {
                    Element compareChild = candidates.remove(0);
                    compareElements(baseChild, compareChild, currentPath, result);
                } else {
                    result.append("Missing element at ").append(currentPath)
                            .append("/").append(tag).append("\n");
                }
            }
        } else if (!baseChildren.isEmpty()) {
            result.append("Missing child elements at ").append(currentPath).append("\n");
        }
    }

    private static List<Element> getElementChildren(Element element) {
        NodeList children = element.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) n);
            }
        }
        return elements;
    }

    private static Map<String, List<Element>> buildElementMap(List<Element> elements) {
        Map<String, List<Element>> map = new LinkedHashMap<>();
        for (Element e : elements) {
            map.computeIfAbsent(e.getTagName(), k -> new ArrayList<>()).add(e);
        }
        return map;
    }

    private static String getTextContent(Node node) {
        StringBuilder text = new StringBuilder();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                text.append(n.getTextContent());
            }
        }
        return text.toString();
    }
}
