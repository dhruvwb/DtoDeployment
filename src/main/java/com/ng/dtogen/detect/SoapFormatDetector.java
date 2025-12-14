package com.ng.dtogen.detect;

import com.ng.dtogen.model.SupportedFormat;
import com.ng.dtogen.util.CaseUtil;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SoapFormatDetector implements FormatDetector {

    @Override
    public boolean canHandle(String payload) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(payload.getBytes()));
            Element root = doc.getDocumentElement();
            return "Envelope".equals(root.getLocalName()) && root.getNamespaceURI().contains("soap");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SupportedFormat getFormat() {
        return SupportedFormat.SOAP;
    }

    @Override
    public Map<String, String> extractFieldMap(String payload) {
        try {
            Map<String, String> map = new LinkedHashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(payload.getBytes()));
            Element body = (Element) doc.getElementsByTagNameNS("*", "Body").item(0);
            if (body != null) {
                NodeList children = body.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element) {
                        extractRecursive((Element) children.item(i), map);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Invalid SOAP", e);
        }
    }

    // Fix: Only use local tag name, not full hierarchical path
    private void extractRecursive(Element element, Map<String, String> map) {
        NodeList children = element.getChildNodes();
        boolean hasElementChildren = false;

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                hasElementChildren = true;
                extractRecursive((Element) children.item(i), map);
            }
        }

        if (!hasElementChildren) {
            String tag = element.getTagName();         // original tag name (e.g., "id")
            String javaField = CaseUtil.toCamel(tag);  // converted to camelCase
            map.put(tag, javaField);                   // Only tag, not full path
        }
    }
}
