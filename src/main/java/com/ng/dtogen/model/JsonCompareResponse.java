// model/JsonCompareResponse.java
package com.ng.dtogen.model;

import java.util.Map;

public class JsonCompareResponse {
    private Map<String, Object> leftHighlighted;
    private Map<String, Object> rightHighlighted;
    private JsonCompareSummary summary;

    public JsonCompareResponse(Map<String, Object> leftHighlighted,
                               Map<String, Object> rightHighlighted,
                               JsonCompareSummary summary) {
        this.leftHighlighted = leftHighlighted;
        this.rightHighlighted = rightHighlighted;
        this.summary = summary;
    }

    public Map<String, Object> getLeftHighlighted() {
        return leftHighlighted;
    }

    public Map<String, Object> getRightHighlighted() {
        return rightHighlighted;
    }

    public JsonCompareSummary getSummary() {
        return summary;
    }
}
