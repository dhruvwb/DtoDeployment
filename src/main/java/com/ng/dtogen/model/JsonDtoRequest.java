package com.ng.dtogen.model;

import lombok.Data;

@Data
public class JsonDtoRequest {
    private Object json;
    private String rootPrefix;
    private String rootClass;  // e.g. "RootRequestDto"
}
