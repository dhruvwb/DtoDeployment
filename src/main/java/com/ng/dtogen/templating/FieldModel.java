package com.ng.dtogen.templating;

public class FieldModel {
    private final String originalName;
    private final String javaName;
    private final String javaType;
    private final boolean needsAnnotation;
    private final boolean hasComma;

    public FieldModel(String originalName, String javaName, String javaType, boolean needsAnnotation, boolean hasComma) {
        this.originalName = originalName;
        this.javaName = javaName;
        this.javaType = javaType;
        this.needsAnnotation = needsAnnotation;
        this.hasComma = hasComma;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getJavaName() {
        return javaName;
    }

    public String getJavaType() {
        return javaType;  // ✅ REQUIRED for {{javaType}} in template
    }

    public boolean isNeedsAnnotation() {
        return needsAnnotation;
    }

    public boolean isHasComma() {
        return hasComma;
    }
}
