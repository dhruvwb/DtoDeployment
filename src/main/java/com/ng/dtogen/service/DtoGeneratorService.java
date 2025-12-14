package com.ng.dtogen.service;

import com.ng.dtogen.model.CompareRequest;

public interface DtoGeneratorService {

        String generateXmlDto(
                        String xml,
                        String rootClassName,
                        String prefix,
                        boolean includeAnnotations) throws Exception;

        String generateJsonDto(
                        Object json,
                        String rootClassName,
                        String prefix,
                        boolean includeAnnotations) throws Exception;

        String generateSoapDto(
                        String xml,
                        String rootClassName,
                        String prefix,
                        boolean includeAnnotations) throws Exception;

        String compareJson(CompareRequest request) throws Exception;
}
