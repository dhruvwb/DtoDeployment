package com.ng.dtogen.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class XmlUtil {

    public static Element parseRoot(String xml) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.getDocumentElement().normalize();
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException("Failed parsing XML", e);
        }
    }

    public static Map<String, Object> elementToMap(Element elem) {
        Map<String, Object> result = new LinkedHashMap<>();
        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) {
                Element child = (Element) n;
                String name = child.getTagName();
                Object value;

                if (child.hasChildNodes() && hasElementChild(child)) {
                    value = elementToMap(child);
                } else {
                    value = child.getTextContent().trim();
                }

                if (result.containsKey(name)) {
                    Object existing = result.get(name);
                    if (existing instanceof List) {
                        ((List<Object>) existing).add(value);
                    } else {
                        List<Object> newList = new ArrayList<>();
                        newList.add(existing);
                        newList.add(value);
                        result.put(name, newList);
                    }
                } else {
                    result.put(name, value);
                }
            }
        }

        return result;
    }

    private static boolean hasElementChild(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                return true;
            }
        }
        return false;
    }
}
