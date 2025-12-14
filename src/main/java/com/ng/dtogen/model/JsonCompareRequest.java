// model/JsonCompareRequest.java
package com.ng.dtogen.model;

import java.util.Map;

public class JsonCompareRequest {
    private Map<String, Object> left;
    private Map<String, Object> right;

    public Map<String, Object> getLeft() {
        return left;
    }

    public void setLeft(Map<String, Object> left) {
        this.left = left;
    }

    public Map<String, Object> getRight() {
        return right;
    }

    public void setRight(Map<String, Object> right) {
        this.right = right;
    }
}
