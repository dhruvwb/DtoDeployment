// model/JsonCompareSummary.java
package com.ng.dtogen.model;

import java.util.List;

public class JsonCompareSummary {
    private List<String> extraKeys;
    private List<String> missingKeys;
    private List<String> valueMismatches;

    public JsonCompareSummary(List<String> extraKeys, List<String> missingKeys, List<String> valueMismatches) {
        this.extraKeys = extraKeys;
        this.missingKeys = missingKeys;
        this.valueMismatches = valueMismatches;
    }

    public List<String> getExtraKeys() {
        return extraKeys;
    }

    public List<String> getMissingKeys() {
        return missingKeys;
    }

    public List<String> getValueMismatches() {
        return valueMismatches;
    }
}
