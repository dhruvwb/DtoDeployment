package com.ng.dtogen.detect;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import com.ng.dtogen.model.SupportedFormat;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;

@Component
public class XmlFormatDetector implements FormatDetector {

    @Override
    public boolean canHandle(String payload) {
        return payload.trim().startsWith("<");
    }

    @Override
    public SupportedFormat getFormat() {
        return SupportedFormat.XML;
    }

    @Override
    public Map<String, String> extractFieldMap(String payload) {
        try {
            String clean = payload.trim();

            if (clean.startsWith("<![CDATA[")) {
                clean = clean.substring(9, clean.length() - 3);
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(clean.getBytes()));

            doc.getDocumentElement().normalize();

            NodeList childNodes = doc.getDocumentElement().getChildNodes();
            Map<String, String> fields = new LinkedHashMap<>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    String tag = childNodes.item(i).getNodeName();
                    fields.put(tag, toCamelCase(tag));
                }
            }
            return fields;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }

    private String toCamelCase(String s) {
        StringBuilder result = new StringBuilder();
        boolean capitalize = false;
        for (char c : s.toCharArray()) {
            if (c == '_' || c == '-') {
                capitalize = true;
            } else {
                result.append(capitalize ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalize = false;
            }
        }
        return result.toString();
    }
}
