package com.ng.dtogen.service;

import com.ng.dtogen.util.JsonComparator;
import com.ng.dtogen.util.XmlComparator;
import org.springframework.stereotype.Service;

@Service
public class CompareService {

    private String basePayload;

    public void setBasePayload(String basePayload) {
        this.basePayload = basePayload;
    }

    public String compareWith(String comparePayload) {
        if (basePayload == null) {
            return "⚠️ Base payload not uploaded yet.";
        }

        if (isJson(basePayload) && isJson(comparePayload)) {
            return JsonComparator.compare(basePayload, comparePayload);
        } else if (isXml(basePayload) && isXml(comparePayload)) {
            return XmlComparator.compare(basePayload, comparePayload);
        } else {
            return "❌ Payload type mismatch or unsupported format.";
        }
    }

    private boolean isJson(String str) {
        return str.trim().startsWith("{");
    }

    private boolean isXml(String str) {
        return str.trim().startsWith("<");
    }
}
