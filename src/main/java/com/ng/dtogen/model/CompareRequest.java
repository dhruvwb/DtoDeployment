package com.ng.dtogen.model;

public class CompareRequest {

    private Object currentJson;
    private Object expectedJson;

    public Object getCurrentJson() {
        return currentJson;
    }

    public void setCurrentJson(Object currentJson) {
        this.currentJson = currentJson;
    }

    public Object getExpectedJson() {
        return expectedJson;
    }

    public void setExpectedJson(Object expectedJson) {
        this.expectedJson = expectedJson;
    }
}
