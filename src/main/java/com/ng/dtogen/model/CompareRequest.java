package com.ng.dtogen.model;

import lombok.Data;

@Data
public class CompareRequest {
    private Object currentJson;
    private Object expectedJson;

  
}
