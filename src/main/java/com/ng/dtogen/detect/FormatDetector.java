package com.ng.dtogen.detect;

import com.ng.dtogen.model.SupportedFormat;

import java.util.Map;

/**
 * Contract for detecting payload formats (JSON, XML, SOAP) and
 * flattening them into a Map of originalName → javaFieldName.
 */
public interface FormatDetector {

    /** Returns true if this detector can parse the given payload. */
    boolean canHandle(String payload);

    /** The format associated with this detector. */
    SupportedFormat getFormat();

    
    
    /**
     * Extracts a flat map of field names. The key is the original name
     * (as it appears in the payload); the value is the camel‑cased Java field
     * name your DTO should use.
     */
    Map<String, String> extractFieldMap(String payload);
    default Map<String, Object> extractJsonTree(String input) {
        throw new UnsupportedOperationException("extractJsonTree not supported for non-JSON format");
    }
}
